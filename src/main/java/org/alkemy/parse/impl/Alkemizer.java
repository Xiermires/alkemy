/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
package org.alkemy.parse.impl;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.AlkemyNode;
import org.alkemy.annotations.Order;
import org.alkemy.util.Assertions;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alkemizer extends ClassVisitor
{
    static final String IS_INSTRUMENTED = "is$$instrumented";
    static final String CREATE_INSTANCE = "create$$instance";

    private static final Pattern DESC = Pattern.compile("^L(.+\\/.+)+;$");
    private static final Logger log = LoggerFactory.getLogger(Alkemizer.class);

    private final List<AlkemizableField> alkemizableFields = new ArrayList<>();
    private final Set<String> alkemizableAnnotations = new HashSet<>();
    private final Set<String> nonAlkemizableAnnotations = new HashSet<>();

    private final String className;
    private boolean ordered;
    private Map<String, Integer> orderedFieldNames = new HashMap<String, Integer>(); // the values inside @Order if defined

    private Alkemizer(String className, ClassVisitor cv)
    {
        super(Opcodes.ASM5, cv);
        this.className = className;
    }

    static byte[] alkemize(String className, byte[] classBytes)
    {
        if (Objects.nonNull(className)) // do not instrument on the fly created classes by for instance Unsafe#define...
        {
            final ClassReader cr = new ClassReader(classBytes);
            final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            try
            {
                cr.accept(new Alkemizer(cr.getClassName(), cw), ClassReader.SKIP_FRAMES);
                return cw.toByteArray();
            }
            catch (Exception e) // error while alkemizing. Return the original class.
            {
            }
        }
        return classBytes;
    }

    static String getGetterName(String fieldName)
    {
        return "get$$" + fieldName;
    }

    static String getSetterName(String fieldName)
    {
        return "set$$" + fieldName;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
    {
        if (Order.class.getName().equals(toQualifiedNameFromDesc(desc)))
        {
            ordered = true;
            return new OrderValueReader(orderedFieldNames, super.visitAnnotation(desc, visible));
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
    {
        final FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        return new FieldAnnotationVisitor(fv, name, desc, alkemizableFields, alkemizableAnnotations, nonAlkemizableAnnotations);
    }

    @Override
    public void visitEnd()
    {
        if (!alkemizableFields.isEmpty())
        {
            appendOrder();
            appendNodeConstructor();
            appendIsInstrumented();
            appendGetters();
            appendSetters();
        }
        super.visitEnd();
    }

    // forces field declaration order if none specified.
    private void appendOrder()
    {
        if (!ordered)
        {
            final AnnotationVisitor av = super.visitAnnotation("Lorg/alkemy/annotations/Order;", true);
            final AnnotationVisitor aav = av.visitArray("value");
            alkemizableFields.forEach(af -> aav.visit(null, af.name));
            aav.visitEnd();
            av.visitEnd();
        }
    }

    private void appendIsInstrumented()
    {
        final String methodName = IS_INSTRUMENTED;
        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()Z", null, null);

        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void appendNodeConstructor()
    {
        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_STATIC, "create$$instance", "([Ljava/lang/Object;)"
                + classNameAsDesc(className), null, null);

        mv.visitCode();
        final Label l0 = new Label();
        mv.visitLabel(l0);

        // Boundary check (throws InvalidArgument).
        mv.visitVarInsn(ALOAD, 0);
        visitArgsPosToLoad(alkemizableFields.size(), mv);
        mv.visitMethodInsn(INVOKESTATIC, "org/alkemy/parse/impl/Alkemizer$Proxy", "ofSize", "([Ljava/lang/Object;I)V", false);

        // TODO 1: document "a visible non-args ctor is required for the instrumented version."
        // TODO 2: detect it and do not create the ctor.
        mv.visitLabel(new Label());
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V", false);

        mv.visitVarInsn(ASTORE, 1);

        // need this label for the local variables
        final Label l2 = new Label();
        mv.visitLabel(l2);

        if (ordered)
        {
            checkFieldNames(alkemizableFields, orderedFieldNames);
            sortByOrder(alkemizableFields, orderedFieldNames);
        }

        for (int i = 0; i < alkemizableFields.size(); i++)
        {
            final AlkemizableField af = alkemizableFields.get(i);

            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            visitArgsPosToLoad(i, mv);
            mv.visitInsn(AALOAD);

            final ClassCaster classCaster = getCastClassForDesc(af.type);
            mv.visitTypeInsn(CHECKCAST, classCaster.name);
            if (classCaster.method != null)
            {
                mv.visitMethodInsn(INVOKEVIRTUAL, classCaster.name, classCaster.method, "()" + af.type, false);
            }
            mv.visitFieldInsn(PUTFIELD, className, af.name, af.type);
        }

        mv.visitLabel(new Label());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);

        final Label ln = new Label();
        mv.visitLabel(ln);

        mv.visitLocalVariable("args", "[Ljava/lang/Object;", null, l0, ln, 0);
        mv.visitLocalVariable("instance", classNameAsDesc(className), null, l2, ln, 1);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void checkFieldNames(List<AlkemizableField> alkemizableFields, Map<String, Integer> orderedFieldNames)
    {
        final List<String> names = alkemizableFields.stream().map(af -> af.name).collect(Collectors.toList());
        if (names.size() != orderedFieldNames.size()) throw new Stop(); // invalid definition

        if (!names.containsAll(orderedFieldNames.keySet())) throw new Stop(); // invalid definition
    }

    private void sortByOrder(List<AlkemizableField> alkemizableFields, Map<String, Integer> orderedFieldNames)
    {
        Collections.sort(alkemizableFields, (lhs, rhs) ->
        {
            return Integer.compare(orderedFieldNames.get(lhs.name), orderedFieldNames.get(rhs.name));
        });
    }

    private void appendGetters()
    {
        for (AlkemizableField af : alkemizableFields)
        {
            appendGetter(af.name, af.type);
        }
    }

    private void appendSetters()
    {
        for (AlkemizableField af : alkemizableFields)
        {
            appendSetter(af.name, af.type);
        }
    }

    private void appendGetter(String name, String desc)
    {
        final String methodName = getGetterName(name);
        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC, methodName, "()" + desc, null, null);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, className, name, desc);
        mv.visitInsn(Type.getType(desc).getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void appendSetter(String name, String desc)
    {
        final String methodName = getSetterName(name);
        final MethodVisitor mv = visitMethod(ACC_PUBLIC, methodName, "(" + desc + ")V", null, null);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(Type.getType(desc).getOpcode(ILOAD), 1);
        mv.visitFieldInsn(PUTFIELD, className, name, desc);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private ClassCaster getCastClassForDesc(String desc)
    {
        if ("D".equals(desc))
        {
            return new ClassCaster("java/lang/Double", "doubleValue");
        }
        else if ("F".equals(desc))
        {
            return new ClassCaster("java/lang/Float", "floatValue");
        }
        else if ("J".equals(desc))
        {
            return new ClassCaster("java/lang/Long", "longValue");
        }
        else if ("I".equals(desc))
        {
            return new ClassCaster("java/lang/Integer", "intValue");
        }
        else if ("S".equals(desc))
        {
            return new ClassCaster("java/lang/Short", "shortValue");
        }
        else if ("B".equals(desc))
        {
            return new ClassCaster("java/lang/Byte", "byteValue");
        }
        else if ("C".equals(desc))
        {
            return new ClassCaster("java/lang/Character", "charValue");
        }
        else if ("Z".equals(desc))
        {
            return new ClassCaster("java/lang/Boolean", "booleanValue");
        }
        else if (desc.startsWith("["))
        {
            return new ClassCaster(desc, null);
        }
        else
        {
            return new ClassCaster(toClassNameFromDesc(desc), null);
        }
    }

    private void visitArgsPosToLoad(int i, MethodVisitor mv)
    {
        if (i == 0)
        {
            mv.visitInsn(ICONST_0);
        }
        else if (i == 1)
        {
            mv.visitInsn(ICONST_1);
        }
        else if (i == 2)
        {
            mv.visitInsn(ICONST_2);
        }
        else if (i == 3)
        {
            mv.visitInsn(ICONST_3);
        }
        else if (i == 4)
        {
            mv.visitInsn(ICONST_4);
        }
        else if (i == 5)
        {
            mv.visitInsn(ICONST_5);
        }
        else if (i <= 127)
        {
            mv.visitIntInsn(BIPUSH, i);
        }
        else
        {
            mv.visitIntInsn(SIPUSH, i);
        }
    }

    // dots
    static String toQualifiedNameFromDesc(String desc)
    {
        return toClassNameFromDesc(desc).replace('/', '.');
    }

    // slashes
    static String toClassNameFromDesc(String desc)
    {
        final Matcher matcher = DESC.matcher(desc);
        if (matcher.matches()) return matcher.group(1);
        return null;
    }

    private String classNameAsDesc(String className)
    {
        return "L" + className + ";";
    }

    static class ClassCaster
    {
        final String name;
        final String method;

        ClassCaster(String className, String castMethod)
        {
            this.name = className;
            this.method = castMethod;
        }
    }

    static class FieldAnnotationVisitor extends FieldVisitor
    {
        private final String name;
        private final String type;
        private final List<AlkemizableField> alkemizables;
        private final Set<String> alkemizableAnnotations;
        private final Set<String> nonAlkemizableAnnotations;

        private AlkemizableField alkemizable;

        FieldAnnotationVisitor(FieldVisitor fv, String name, String type, List<AlkemizableField> alkemizables,
                Set<String> alkemizableAnnotations, Set<String> nonAlkemizableAnnotations)
        {
            super(Opcodes.ASM5, fv);

            this.name = name;
            this.type = type;
            this.alkemizables = alkemizables;
            this.alkemizableAnnotations = alkemizableAnnotations;
            this.nonAlkemizableAnnotations = nonAlkemizableAnnotations;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            if (visible && isAlkemizable(desc))
            {
                if (alkemizable == null)
                {
                    alkemizable = new AlkemizableField(name, type);
                }
            }
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public void visitEnd()
        {
            if (alkemizable != null)
            {
                alkemizables.add(alkemizable);
            }
            super.visitEnd();
        }

        private boolean isAlkemizable(String desc)
        {
            return !nonAlkemizableAnnotations.contains(desc)
                    && (alkemizableAnnotations.contains(desc) || AlkemyNode.class.getName().equals(toQualifiedNameFromDesc(desc)) || isAnnotationPresent(
                            desc, AlkemyLeaf.class));
        }

        private boolean isAnnotationPresent(String desc, Class<? extends Annotation> clazz)
        {
            final String qualifiedName = toQualifiedNameFromDesc(desc);
            try
            {
                final ClassReader cr = new ClassReader(qualifiedName);
                final FindAnnotation cv = new FindAnnotation(clazz, nonAlkemizableAnnotations);
                cr.accept(cv, ClassReader.SKIP_CODE);

                if (cv.annotated)
                {
                    alkemizableAnnotations.add(desc);
                }
                return cv.annotated;
            }
            catch (IOException e)
            {
                nonAlkemizableAnnotations.add(desc);
                log.trace("Cannot read the annotation '%s'. Ignore.", desc);
            }
            return false;
        }
    };

    static class FindAnnotation extends ClassVisitor
    {
        private final Class<? extends Annotation> annotation;
        private boolean annotated = false;
        private final Set<String> nonAlkemizableAnnotations;

        FindAnnotation(Class<? extends Annotation> annotation, Set<String> nonAlkemizableAnnotations)
        {
            super(Opcodes.ASM5);

            assert annotation.isAnnotation() : "Provided class isn't an annotation.";
            this.annotation = annotation;
            this.nonAlkemizableAnnotations = nonAlkemizableAnnotations;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            if (annotation.getName().equals(toQualifiedNameFromDesc(desc)))
            {
                annotated = true;
            }
            else
            {
                nonAlkemizableAnnotations.add(desc);
            }
            return super.visitAnnotation(desc, visible);
        }
    }

    static class OrderValueReader extends AnnotationVisitor
    {
        int i = 0;
        Map<String, Integer> m;

        public OrderValueReader(Map<String, Integer> m, AnnotationVisitor av)
        {
            super(Opcodes.ASM5, av);
            this.m = m;
        }

        @Override
        public AnnotationVisitor visitArray(String name)
        {
            return new OrderValueReader(m, super.visitArray(name));
        }

        @Override
        public void visit(String name, Object value)
        {
            m.put(String.valueOf(value), i++);
            super.visit(name, value);
        }
    }

    static class AlkemizableField
    {
        private final String name;
        private final String type;

        AlkemizableField(String name, String type)
        {
            this.name = name;
            this.type = type;
        }
    }

    // Compile check so changing conditions doesn't miss this class.
    // If changed, change also the instrumentation boundary check.
    public static class Proxy
    {
        public static void ofSize(Object[] o, int i)
        {
            Assertions.ofSize(o, i);
        }
    }

    static class Stop extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }
}
