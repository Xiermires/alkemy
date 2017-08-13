/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.alkemy.instr;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.SIPUSH;

import java.util.List;
import java.util.Map;

import org.alkemy.instr.DefaultAlkemizableVisitor.FieldProperties;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ConstructorWriter
{
    public static final String CREATE_DEFAULT = "create$$default";
    public static final String INSTATIATOR = "obj$$instantiator";
    public static final String CREATE_ARGS = "create$$args";
    
    private ConstructorWriter() {
    }
    
    static void appendCreateDefault(ClassVisitor cv, String className, boolean defaultCtor)
    {
        if (!defaultCtor)
        {
            final FieldVisitor fv = cv.visitField(ACC_PRIVATE, INSTATIATOR, "Lorg/objenesis/instantiator/ObjectInstantiator;",
                    "Lorg/objenesis/instantiator/ObjectInstantiator<" + AlkemizerUtils.toDescFromClassName(className) + ">;",
                    null);
            fv.visitEnd();
        }

        final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_DEFAULT, //
                "()" + AlkemizerUtils.toDescFromClassName(className), null, null);

        mv.visitCode();
        final Label l0 = new Label();
        mv.visitLabel(l0);

        if (defaultCtor)
        {
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V", false);
            mv.visitInsn(ARETURN);
        }
        else
        {
            mv.visitFieldInsn(GETSTATIC, className, "instantiator", "Lorg/objenesis/instantiator/ObjectInstantiator;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/objenesis/instantiator/ObjectInstantiator", "newInstance",
                    "()Ljava/lang/Object;", true);
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitInsn(ARETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public static void appendCreateArgs(ClassWriter cw, String className, List<String> orderedNames, Map<String, FieldProperties> fieldMap)
    {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_ARGS, //
                "([Ljava/lang/Object;)" + AlkemizerUtils.toDescFromClassName(className), null, null);

        mv.visitCode();

        final Label l0 = new Label();
        mv.visitLabel(l0);

        mv.visitMethodInsn(INVOKESTATIC, className, CREATE_DEFAULT, //
                "()" + AlkemizerUtils.toDescFromClassName(className), false);
        mv.visitVarInsn(ASTORE, 1);

        for (int i = 0; i < orderedNames.size(); i++)
        {
            final String name = orderedNames.get(i);
            final FieldProperties props = fieldMap.get(name);

            mv.visitVarInsn(ALOAD, 1);
            if (props.isEnum)
            {
                mv.visitLdcInsn(Type.getType(props.desc));
            }
            mv.visitVarInsn(ALOAD, 0);
            visitInStack(i, mv);
            mv.visitInsn(AALOAD);

            final ClassCaster classCaster = getCastClassForDesc(props.desc);
            if (props.isEnum)
            {
                mv.visitMethodInsn(INVOKESTATIC, "org/alkemy/parse/impl/AlkemizerUtils$Proxy", "toEnum",
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
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static ClassCaster getCastClassForDesc(String desc)
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

    private static void visitInStack(int i, MethodVisitor mv)
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
