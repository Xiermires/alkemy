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
package org.alkemy.alkemizer;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.AlkemyNode;
import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Alkemizer extends ClassVisitor
{
    public static final String IS_INSTRUMENTED = "is$$instrumented";

    private static final Logger log = Logger.getLogger(Alkemizer.class);

    private final List<AlkemizableField> alkemizableFields = new ArrayList<>();
    private final Set<String> alkemizableAnnotations = new HashSet<>();
    private final Set<String> nonAlkemizableAnnotations = new HashSet<>();

    private final String className;

    private Alkemizer(String className, ClassVisitor cv)
    {
        super(Opcodes.ASM5, cv);

        this.className = className;
    }

    public static byte[] alkemize(byte[] clazz)
    {
        final ClassReader cr = new ClassReader(clazz);
        final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(new Alkemizer(cr.getClassName(), cw), ClassReader.SKIP_FRAMES);
        return cw.toByteArray();
    }

    public static String getGetterName(String fieldName)
    {
        return "get$$" + fieldName;
    }

    public static String getSetterName(String fieldName)
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
            appendIsInstrumented();
            appendGetters();
            appendSetters();
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
    }

    private static class FieldAnnotationVisitor extends FieldVisitor
    {
        private static final Pattern DESC = Pattern.compile("^L(.+\\/.+)+;$");

        private final String name;
        private final String type;
        private final List<AlkemizableField> alkemizables;
        private final Set<String> alkemizableAnnotations;
        private final Set<String> nonAlkemizableAnnotations;

        FieldAnnotationVisitor(FieldVisitor fv, String name, String type, List<AlkemizableField> alkemizables, Set<String> alkemizableAnnotations,
                Set<String> nonAlkemizableAnnotations)
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
                alkemizables.add(new AlkemizableField(name, type));
            }
            return super.visitAnnotation(desc, visible);
        }

        private boolean isAlkemizable(String desc)
        {
            return !nonAlkemizableAnnotations.contains(desc)
                    && (alkemizableAnnotations.contains(desc) || AlkemyNode.class.getName().equals(getAnnotationQualifiedName(desc)) || isAnnotationPresent(desc,
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
                final FindAnnotation cv = new FindAnnotation(clazz, alkemizableAnnotations, nonAlkemizableAnnotations);
                cr.accept(cv, ClassReader.SKIP_CODE);
                return cv.annotated;
            }
            catch (IOException e)
            {
                log.debug("Cannot read the annotation. Ignore.", e);
            }
            return false;
        }
    };

    private static class FindAnnotation extends ClassVisitor
    {
        private final Class<? extends Annotation> annotation;
        private boolean annotated = false;
        private final Set<String> alkemizableAnnotations;
        private final Set<String> nonAlkemizableAnnotations;

        FindAnnotation(Class<? extends Annotation> annotation, Set<String> alkemizableAnnotations, Set<String> nonAlkemizableAnnotations)
        {
            super(Opcodes.ASM5);

            assert annotation.isAnnotation() : "Provided class isn't an annotation.";
            this.annotation = annotation;
            this.alkemizableAnnotations = alkemizableAnnotations;
            this.nonAlkemizableAnnotations = nonAlkemizableAnnotations;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            if (annotation.getName().equals(FieldAnnotationVisitor.getAnnotationQualifiedName(desc)))
            {
                annotated = true;
                alkemizableAnnotations.add(desc);
            }
            else
            {
                nonAlkemizableAnnotations.add(desc);
            }
            return super.visitAnnotation(desc, visible);
        }
    }

    private static class AlkemizableField
    {
        private final String name;
        private final String type;

        AlkemizableField(String name, String type)
        {
            this.name = name;
            this.type = type;
        }
    }
}
