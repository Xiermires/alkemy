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
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
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
import static org.objectweb.asm.Opcodes.PUTSTATIC;
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
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.Order;
import org.alkemy.util.AlkemyUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instrumentation class focused on the alkemization of fields.
 * <p>
 * It does the following:
 * <ul>
 * <li>Creates a marker method : 'public static boolean is$$instrumented() { return true; }' (this
 * can allow enabling / disabling the instr. version on runtime).
 * <li>Creates an {@link Order} annotation with the declaration order of the fields, or leave it
 * untouched if present.
 * <li>Makes the default constructor public if it isn't already.
 * <li>Creates a public static factory for the type : 'public static TypeClass
 * create$$instance(Object[] args) { ... }', where the args follow the order established in the
 * {@link Order} annotation.
 * <li>Creates for each alkemized member a getter and a setter 'public fieldType get$$fieldName() {
 * ... }' && 'public void set$$fieldName(fieldType newValue) { ... }'
 * <li>Creates for each alkemized static member a getter and a setter 'public static fieldType
 * get$$fieldName() { ... }' && 'public static void set$$fieldName(fieldType newValue) { ... }'
 * <li>Conversions && castings (wrapper -> primitive && String -> enum).
 * </ul>
 */
// TODO clean up this code
public class Alkemizer extends ClassVisitor
{
    static final String IS_INSTRUMENTED = "is$$instrumented";
    static final String CREATE_INSTANCE = "create$$instance";

    private static final Pattern DESC = Pattern.compile("^L(.+\\/.+)+;$");
    private static final Logger log = LoggerFactory.getLogger(Alkemizer.class); // TODO why is
                                                                                // LogBack not
                                                                                // formatting

    private final List<AlkemizableField> alkemizableFields = new ArrayList<>();
    private final Set<String> alkemizableAnnotations = new HashSet<>();
    private final Set<String> nonAlkemizableAnnotations = new HashSet<>();

    private final String className;
    private boolean isOrdered; // contains Order annotation
    private boolean hasDefaultCtor;
    private Map<String, Integer> orderedFieldNames = new HashMap<>(); // the values
                                                                      // inside
                                                                      // @Order if
                                                                      // defined

    private Alkemizer(String className, ClassVisitor cv)
    {
        super(ASM5, cv);
        this.className = className;
    }

    static byte[] alkemize(String className, byte[] classBytes)
    {
        if (Objects.nonNull(className)) // do not instrument on the fly created classes by for
                                        // instance Unsafe#define...
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
                log.trace("Error while alkemizing. Keep non modified version.", e);
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
            isOrdered = true;
            return new OrderValueReader(orderedFieldNames, super.visitAnnotation(desc, visible));
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        int _access = access;
        if (isDefaultCtor(name, desc))
        {
            hasDefaultCtor = true;
            // if not accessible, make it public
            if (_access == 0) // package default
            {
                _access += ACC_PUBLIC;
            }
            if ((_access & ACC_PRIVATE) != 0)
            {
                _access = (_access - ACC_PRIVATE) + ACC_PUBLIC;
            }
            else if ((_access & ACC_PROTECTED) != 0)
            {
                _access = (_access - ACC_PROTECTED) + ACC_PUBLIC;
            }
        }
        return super.visitMethod(_access, name, desc, signature, exceptions);
    }

    private static boolean isDefaultCtor(String name, String desc)
    {
        return "<init>".equals(name) && "()V".equals(desc);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
    {
        boolean isStatic = (access & ACC_STATIC) != 0;
        // If field is an object, check if it is an enum.
        boolean isEnum = false;
        if (desc.startsWith("L"))
        {
            try
            {
                final ClassReader cr = new ClassReader(toQualifiedNameFromDesc(desc));
                final FindEnum cv = new FindEnum();
                cr.accept(cv, ClassReader.SKIP_CODE);
                isEnum = cv.isEnum;
            }
            catch (IOException e)
            {
                throw new Stop();
            }
        }

        final FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        return new FieldAnnotationVisitor(fv, name, desc, alkemizableFields, alkemizableAnnotations, nonAlkemizableAnnotations,
                isEnum, isStatic);
    }

    @Override
    public void visitEnd()
    {
        if (!hasDefaultCtor)
        {
            log.debug("Alkemization failed. Trying to alkemize type : '%s' without default constructor.", className);
            throw new Stop();
        }

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
        if (!isOrdered)
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
                + toDescFromClassName(className), null, null);

        mv.visitCode();

        final Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V", false);

        mv.visitVarInsn(ASTORE, 1);

        // need this label for the local variables
        final Label l2 = new Label();
        mv.visitLabel(l2);

        if (isOrdered)
        {
            checkFieldNames(alkemizableFields, orderedFieldNames);
            sortByOrder(alkemizableFields, orderedFieldNames);
        }

        for (int i = 0; i < alkemizableFields.size(); i++)
        {
            final AlkemizableField af = alkemizableFields.get(i);

            mv.visitVarInsn(ALOAD, 1);
            if (af.isEnum)
            {
                mv.visitLdcInsn(Type.getType(af.type));
            }
            mv.visitVarInsn(ALOAD, 0);
            visitArgsPosToLoad(i, mv);
            mv.visitInsn(AALOAD);

            final ClassCaster classCaster = getCastClassForDesc(af.type);
            if (af.isEnum)
            {
                mv.visitMethodInsn(INVOKESTATIC, "org/alkemy/parse/impl/Alkemizer$Proxy", "toEnum",
                        "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            }
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
        mv.visitLocalVariable("instance", toDescFromClassName(className), null, l2, ln, 1);

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
            appendGetter(af.name, af.type, af.isStatic);
        }
    }

    private void appendSetters()
    {
        for (AlkemizableField af : alkemizableFields)
        {
            appendSetter(af.name, af.type, af.isEnum, af.isStatic);
        }
    }

    private void appendGetter(String name, String desc, boolean isStatic)
    {
        final String methodName = getGetterName(name);
        final MethodVisitor mv = super.visitMethod(isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC, methodName, "()" + desc,
                null, null);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(isStatic ? GETSTATIC : GETFIELD, className, name, desc);
        mv.visitInsn(Type.getType(desc).getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void appendSetter(String name, String desc, boolean isEnum, boolean isStatic)
    {
        final String methodName = getSetterName(name);
        final MethodVisitor mv = visitMethod(isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC, methodName, "("
                + (isEnum ? "Ljava/lang/Object;" : desc) + ")V", null, null);

        mv.visitVarInsn(ALOAD, 0);
        if (isEnum)
        {
            mv.visitLdcInsn(Type.getType("Lorg/alkemy/parse/impl/TestAlkemizer$Lorem;"));
        }
        mv.visitVarInsn(Type.getType(desc).getOpcode(ILOAD), 1);
        if (isEnum)
        {
            mv.visitMethodInsn(INVOKESTATIC, "org/alkemy/parse/impl/Alkemizer$Proxy", "toEnum",
                    "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, toClassNameFromDesc(desc));
        }
        mv.visitFieldInsn(isStatic ? PUTSTATIC : PUTFIELD, className, name, desc);
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

    private static String toQualifiedName(String className)
    {
        return className.replace('/', '.');
    }

    // dots
    private static String toQualifiedNameFromDesc(String desc)
    {
        return toQualifiedName(toClassNameFromDesc(desc));
    }

    // slashes
    private static String toClassNameFromDesc(String desc)
    {
        final Matcher matcher = DESC.matcher(desc);
        if (matcher.matches()) return matcher.group(1);
        return null;
    }

    private static String toDescFromClassName(String className)
    {
        return "L" + className + ";";
    }

    static class ClassCaster
    {
        private final String name;
        private final String method;

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
        private final boolean isEnum;
        private final boolean isStatic;

        private AlkemizableField alkemizable;

        FieldAnnotationVisitor(FieldVisitor fv, String name, String type, List<AlkemizableField> alkemizables,
                Set<String> alkemizableAnnotations, Set<String> nonAlkemizableAnnotations, boolean isEnum, boolean isStatic)
        {
            super(ASM5, fv);

            this.name = name;
            this.type = type;
            this.alkemizables = alkemizables;
            this.alkemizableAnnotations = alkemizableAnnotations;
            this.nonAlkemizableAnnotations = nonAlkemizableAnnotations;
            this.isEnum = isEnum;
            this.isStatic = isStatic;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            if (visible && isAlkemizable(desc))
            {
                if (alkemizable == null)
                {
                    alkemizable = new AlkemizableField(name, type, isEnum, isStatic);
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
            else
            {
                final String qualifiedName = toQualifiedNameFromDesc(type);
                try
                {
                    // Avoid cycles
                    final Set<String> visited = new HashSet<String>();
                    visited.add(type);

                    final ClassReader cr = new ClassReader(qualifiedName);
                    final LeafFound leafFound = new LeafFound();
                    final FindLeaf cv = new FindLeaf(visited, alkemizableAnnotations, nonAlkemizableAnnotations, leafFound);
                    cr.accept(cv, ClassReader.SKIP_CODE);
                    if (leafFound.get())
                    {
                        alkemizables.add(new AlkemizableField(name, type, isEnum, isStatic));
                    }
                }
                catch (IOException e)
                {
                    log.trace("Cannot read the annotation '%s'. Ignore.", name);
                }
            }
            super.visitEnd();
        }

        private boolean isAlkemizable(String desc)
        {
            return !nonAlkemizableAnnotations.contains(desc)
                    && (alkemizableAnnotations.contains(desc) | isAnnotationPresent(desc, AlkemyLeaf.class));
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

    static class FindEnum extends ClassVisitor
    {
        private boolean isEnum = false;

        FindEnum()
        {
            super(ASM5);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            isEnum = (access & ACC_ENUM) != 0;
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    static class FindAnnotation extends ClassVisitor
    {
        private final Class<? extends Annotation> annotation;
        private boolean annotated = false;
        private final Set<String> nonAlkemizableAnnotations;

        FindAnnotation(Class<? extends Annotation> annotation, Set<String> nonAlkemizableAnnotations)
        {
            super(ASM5);

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

    static class FindLeaf extends ClassVisitor
    {
        private final Set<String> visited;
        private final Set<String> alkemizableAnnotations;
        private final Set<String> nonAlkemizableAnnotations;
        private final LeafFound leafFound;

        public FindLeaf(Set<String> visited, Set<String> alkemizableAnnotations, Set<String> nonAlkemizableAnnotations,
                LeafFound leafFound)
        {
            super(ASM5);

            this.visited = visited;
            this.alkemizableAnnotations = alkemizableAnnotations;
            this.nonAlkemizableAnnotations = nonAlkemizableAnnotations;
            this.leafFound = leafFound;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
        {
            return new FieldLeafVisitor(visited, name, desc, alkemizableAnnotations, nonAlkemizableAnnotations, leafFound);
        }
    }

    static class FieldLeafVisitor extends FieldVisitor
    {
        private final Set<String> visited;
        private final String name;
        private final String type;
        private final Set<String> alkemizableAnnotations;
        private final Set<String> nonAlkemizableAnnotations;
        private final LeafFound leafFound;

        FieldLeafVisitor(Set<String> visited, String name, String type, Set<String> alkemizableAnnotations,
                Set<String> nonAlkemizableAnnotations, LeafFound leafFound)
        {
            super(ASM5);

            this.visited = visited;
            this.name = name;
            this.type = type;
            this.alkemizableAnnotations = alkemizableAnnotations;
            this.nonAlkemizableAnnotations = nonAlkemizableAnnotations;
            this.leafFound = leafFound;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            if (visible && isAlkemizable(desc))
            {
                leafFound.found = true;
            }
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public void visitEnd()
        {
            if (leafFound.get())
            {
                super.visitEnd();
            }
            else if (!visited.contains(type) && type.startsWith("L"))
            {
                visited.add(type);
                final String qualifiedName = toQualifiedNameFromDesc(type);
                try
                {
                    final ClassReader cr = new ClassReader(qualifiedName);
                    final FindLeaf cv = new FindLeaf(visited, alkemizableAnnotations, nonAlkemizableAnnotations, leafFound);
                    cr.accept(cv, ClassReader.SKIP_CODE);
                }
                catch (IOException e)
                {
                    log.trace("Cannot read the annotation '%s'. Ignore.", name);
                }
            }
        }

        private boolean isAlkemizable(String desc)
        {
            return !nonAlkemizableAnnotations.contains(desc)
                    && (alkemizableAnnotations.contains(desc) | isAnnotationPresent(desc, AlkemyLeaf.class));
        }

        private boolean isAnnotationPresent(String desc, Class<? extends Annotation> clazz)
        {
            final String qualifiedName = toQualifiedNameFromDesc(desc);
            try
            {
                final ClassReader cr = new ClassReader(qualifiedName);
                final FindAnnotation cv = new FindAnnotation(clazz, nonAlkemizableAnnotations);
                cr.accept(cv, ClassReader.SKIP_CODE);
                return cv.annotated;
            }
            catch (IOException e)
            {
                log.trace("Cannot read the annotation '%s'. Ignore.", desc);
            }
            return false;
        }
    };

    static class OrderValueReader extends AnnotationVisitor
    {
        private int i = 0;
        private Map<String, Integer> m;

        public OrderValueReader(Map<String, Integer> m, AnnotationVisitor av)
        {
            super(ASM5, av);
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

    static class LeafFound implements Supplier<Boolean>
    {
        boolean found;

        @Override
        public Boolean get()
        {
            return found;
        }
    }

    static class AlkemizableField
    {
        private final String name;
        private final String type;
        private final boolean isEnum;
        private final boolean isStatic;

        AlkemizableField(String name, String type, boolean isEnum, boolean isStatic)
        {
            this.name = name;
            this.type = type;
            this.isEnum = isEnum;
            this.isStatic = isStatic;
        }
    }

    // Compile check so changing proxy'ed classes doesn't miss this class.
    // If any changes in the behaviour, change also the instrumentation accordingly.
    public static class Proxy
    {
        public static Object toEnum(Class<?> type, Object value)
        {
            return AlkemyUtils.toEnum(type, value);
        }
    }

    static class Stop extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }
}
