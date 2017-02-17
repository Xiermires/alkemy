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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.AlkemyNode;
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

import com.google.common.collect.Table;
import com.google.common.collect.Tables;

public class Alkemizer extends ClassVisitor
{
    static final String IS_INSTRUMENTED = "is$$instrumented";
    static final String CREATE_INSTANCE = "create$$instance";

    private static final Object NO_PROPERTY = new Object(); // indicates an annotation has no properties.

    private static final Logger log = LoggerFactory.getLogger(Alkemizer.class);

    private final List<AlkemizableField> alkemizableFields = new ArrayList<>();
    private final Set<String> alkemizableAnnotations = new HashSet<>();
    private final Set<String> nonAlkemizableAnnotations = new HashSet<>();

    private final String className;

    private Alkemizer(String className, ClassVisitor cv)
    {
        super(Opcodes.ASM5, cv);
        this.className = className;
    }

    static byte[] alkemize(String className, byte[] classBytes)
    {
        final ClassReader cr = new ClassReader(classBytes);
        final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(new Alkemizer(cr.getClassName(), cw), ClassReader.SKIP_FRAMES);
        return cw.toByteArray();
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
            try
            {
                appendFactory();
                appendIsInstrumented();
                appendGetters();
                appendSetters();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        super.visitEnd();
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

    private void appendFactory()
    {
        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_STATIC, "create$$instance", parameterSignature().toString(),
                null, null);

        for (int i = 0; i < alkemizableFields.size(); i++)
        {
            for (Entry<String, Map<String, Object>> outer : alkemizableFields.get(i).annotations.rowMap().entrySet())
            {
                final AnnotationVisitor av = mv.visitParameterAnnotation(i, outer.getKey(), true);
                for (Entry<String, Object> inner : outer.getValue().entrySet())
                {
                    if (NO_PROPERTY == inner.getValue()) continue;

                    av.visit(inner.getKey(), inner.getValue());
                }
                av.visitEnd();
            }
        }

        mv.visitCode();
        final Label l0 = new Label();
        mv.visitLabel(l0);

        // TODO 1: document "a visible non-args ctor is required for the instrumented version."
        // TODO 2: detect it and do not create the ctor.
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V", false);

        final int instancePos = findStorePosition();
        mv.visitVarInsn(ASTORE, instancePos);

        // need this label for the local variables
        final Label l1 = new Label();
        mv.visitLabel(l1);

        mv.visitVarInsn(ALOAD, instancePos);
        mv.visitVarInsn(Type.getType(alkemizableFields.get(0).type).getOpcode(ILOAD), 0);
        mv.visitFieldInsn(PUTFIELD, className, alkemizableFields.get(0).name, alkemizableFields.get(0).type);

        final int offset = getTypeOffset(alkemizableFields.get(0).type);

        for (int i = 1, j = 1 + offset; i < alkemizableFields.size(); i++, j++)
        {
            final AlkemizableField af = alkemizableFields.get(i);

            mv.visitLabel(new Label());
            mv.visitVarInsn(ALOAD, instancePos);
            mv.visitVarInsn(Type.getType(af.type).getOpcode(ILOAD), j);
            mv.visitFieldInsn(PUTFIELD, className, af.name, af.type);

            j += getTypeOffset(af.type);
        }

        mv.visitLabel(new Label());
        mv.visitVarInsn(ALOAD, instancePos);
        mv.visitInsn(ARETURN);

        final Label ln = new Label();
        mv.visitLabel(ln);

        for (int i = 0, j = 0; i < alkemizableFields.size(); i++, j++)
        {
            final AlkemizableField af = alkemizableFields.get(i);
            mv.visitLocalVariable("arg" + i, af.type, null, l0, ln, j);
            j += getTypeOffset(af.type);
        }
        mv.visitLocalVariable("instance", classNameAsDesc(className), null, l1, ln, instancePos);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private int getTypeOffset(String desc)
    {
        return "D".equals(desc) || "J".equals(desc) ? 1 : 0; // double && long take two spots.
    }

    private int findStorePosition()
    {
        int pos = 0;
        for (int i = 0; i < alkemizableFields.size(); i++, pos++)
        {
            pos += getTypeOffset(alkemizableFields.get(i).type);
        }
        return pos;
    }

    private StringBuilder parameterSignature()
    {
        final StringBuilder sb = new StringBuilder().append("(");
        for (AlkemizableField af : alkemizableFields)
        {
            sb.append(Type.getType(af.type));
        }
        sb.append(")").append(classNameAsDesc(className));
        return sb;
    }

    private String classNameAsDesc(String className)
    {
        return "L" + className + ";";
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

    private static class FieldAnnotationVisitor extends FieldVisitor
    {
        private static final Pattern DESC = Pattern.compile("^L(.+\\/.+)+;$");

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
                alkemizable.annotations.put(desc, "", NO_PROPERTY); // In case the annotation has no properties, we still must
                                                                    // attach to the ctor parameter.
                return new ReadAnnotation(alkemizable, desc, super.visitAnnotation(desc, visible));
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
                    && (alkemizableAnnotations.contains(desc)
                            || AlkemyNode.class.getName().equals(getAnnotationQualifiedName(desc)) || isAnnotationPresent(desc,
                                AlkemyLeaf.class));
        }

        private static String getAnnotationQualifiedName(String desc)
        {
            final Matcher matcher = DESC.matcher(desc);
            if (matcher.matches()) return matcher.group(1).replace('/', '.');
            return null;
        }

        private boolean isAnnotationPresent(String desc, Class<? extends Annotation> clazz)
        {
            final String qualifiedName = getAnnotationQualifiedName(desc);
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

    private static class FindAnnotation extends ClassVisitor
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
            if (annotation.getName().equals(FieldAnnotationVisitor.getAnnotationQualifiedName(desc)))
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

    private static class ReadAnnotation extends AnnotationVisitor
    {
        private final AlkemizableField af;
        private final String desc;

        public ReadAnnotation(AlkemizableField af, String desc, AnnotationVisitor annotationVisitor)
        {
            super(Opcodes.ASM5, annotationVisitor);

            this.af = af;
            this.desc = desc;
        }

        @Override
        public void visit(String name, Object value)
        {
            af.annotations.put(desc, name, value);
            super.visit(name, value);
        }
    }

    private static class AlkemizableField
    {
        private final String name;
        private final String type;
        private final Table<String, String, Object> annotations; // annotation desc / property name / value

        AlkemizableField(String name, String type)
        {
            this.name = name;
            this.type = type;
            this.annotations = Tables.newCustomTable(new LinkedHashMap<>(), LinkedHashMap::new);
        }
    }
}
