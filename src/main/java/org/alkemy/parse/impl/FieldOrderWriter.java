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
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.SIPUSH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.alkemy.annotations.Order;
import org.alkemy.parse.impl.Alkemizer.Stop;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class FieldOrderWriter extends FieldAccessorWriter
{
    static final String CREATE_INSTANCE = "create$$instance";

    private boolean ordered;
    private final List<String> orderedFields = new ArrayList<>();

    public FieldOrderWriter(ClassVisitor cv, String className)
    {
        super(cv, className);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
    {
        if (Order.class.getName().equals(AlkemizerUtils.toQualifiedNameFromDesc(desc)))
        {
            ordered = true;
            return new OrderValueReader(orderedFields, super.visitAnnotation(desc, visible));
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd()
    {
        appendOrder();
        appendNodeConstructor();
        super.visitEnd();
    }

    // forces field declaration order if none specified.
    private void appendOrder()
    {
        if (!ordered)
        {
            final AnnotationVisitor av = super.visitAnnotation("Lorg/alkemy/annotations/Order;", true);
            final AnnotationVisitor aav = av.visitArray("value");
            fieldMap.entrySet().stream().filter(entry -> entry.getValue().alkemizable).map(entry -> entry.getKey())
                    .forEach(name -> aav.visit(null, name));
            aav.visitEnd();
            av.visitEnd();
        }
    }

    private void appendNodeConstructor()
    {
        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_STATIC, "create$$instance", "([Ljava/lang/Object;)"
                + AlkemizerUtils.toDescFromClassName(className), null, null);

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

        final List<String> fields = fieldMap.entrySet().stream().filter(entry -> entry.getValue().alkemizable)
                .map(entry -> entry.getKey()).collect(Collectors.toList());

        if (ordered)
        {
            checkFieldNames(fields, orderedFields);
            sortByOrder(fields, orderedFields);
        }

        for (int i = 0; i < fields.size(); i++)
        {
            final String name = fields.get(i);
            final FieldProperties props = fieldMap.get(name);

            mv.visitVarInsn(ALOAD, 1);
            if (props.isEnum)
            {
                mv.visitLdcInsn(Type.getType(props.desc));
            }
            mv.visitVarInsn(ALOAD, 0);
            visitArgsPosToLoad(i, mv);
            mv.visitInsn(AALOAD);

            final ClassCaster classCaster = getCastClassForDesc(props.desc);
            if (props.isEnum)
            {
                mv.visitMethodInsn(INVOKESTATIC, "org/alkemy/parse/impl/AbstractClassFieldVisitor$Proxy", "toEnum",
                        "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            }
            mv.visitTypeInsn(CHECKCAST, classCaster.name);
            if (classCaster.method != null)
            {
                mv.visitMethodInsn(INVOKEVIRTUAL, classCaster.name, classCaster.method, "()" + props.desc, false);
            }
            mv.visitFieldInsn(PUTFIELD, className, name, props.desc);
        }

        mv.visitLabel(new Label());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);

        final Label ln = new Label();
        mv.visitLabel(ln);

        mv.visitLocalVariable("args", "[Ljava/lang/Object;", null, l0, ln, 0);
        mv.visitLocalVariable("instance", AlkemizerUtils.toDescFromClassName(className), null, l2, ln, 1);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void checkFieldNames(Collection<String> fields, List<String> orderedFields)
    {
        if (fields.size() != orderedFields.size()) throw new Stop(); // invalid definition
        if (!fields.containsAll(orderedFields)) throw new Stop(); // invalid definition
    }

    private void sortByOrder(List<String> fields, List<String> orderedFields)
    {
        Collections.sort(fields, (lhs, rhs) ->
        {
            return Integer.compare(orderedFields.indexOf(lhs), orderedFields.indexOf(rhs));
        });
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
            return new ClassCaster(AlkemizerUtils.toClassNameFromDesc(desc), null);
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

    static class OrderValueReader extends AnnotationVisitor
    {
        private List<String> fields;

        public OrderValueReader(List<String> fields, AnnotationVisitor av)
        {
            super(ASM5, av);
            this.fields = fields;
        }

        @Override
        public AnnotationVisitor visitArray(String name)
        {
            return new OrderValueReader(fields, super.visitArray(name));
        }

        @Override
        public void visit(String name, Object value)
        {
            fields.add(String.valueOf(value));
            super.visit(name, value);
        }
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
}
