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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.List;
import java.util.Map;

import org.alkemy.instr.DefaultAlkemizableVisitor.FieldProperties;
import org.alkemy.instr.DefaultAlkemizableVisitor.MethodProperties;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class GetterSetterWriter
{
    private GetterSetterWriter()
    {
    }

    public static void appendGetters(ClassWriter cw, String className, List<String> fieldNames,
            Map<String, FieldProperties> fieldMap, Map<String, MethodProperties> methodMap)
    {
        for (String fieldName : fieldNames)
        {
            final String methodName = AlkemizerUtils.getGetterName(fieldName);
            appendGetter(cw, className, fieldName, fieldMap.get(fieldName), methodName, methodMap.get(methodName));
        }
    }

    public static void appendSetters(ClassWriter cw, String className, List<String> fieldNames,
            Map<String, FieldProperties> fieldMap, Map<String, MethodProperties> methodMap)
    {
        for (String fieldName : fieldNames)
        {
            final String methodName = AlkemizerUtils.getSetterName(fieldName);
            appendSetter(cw, className, fieldName, fieldMap.get(fieldName), methodName, methodMap.get(methodName));
        }
    }

    private static void appendGetter(ClassWriter cw, String className, String fieldName, FieldProperties fieldProperties,
            String methodName, MethodProperties methodProperties)
    {
        if (methodProperties == null || !("()" + fieldProperties.desc).equals(methodProperties.desc))
        {
            final MethodVisitor mv = cw.visitMethod(fieldProperties.isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC, methodName,
                    "()" + fieldProperties.desc, null, null);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(fieldProperties.isStatic ? GETSTATIC : GETFIELD, className, fieldName, fieldProperties.desc);
            mv.visitInsn(Type.getType(fieldProperties.desc).getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private static void appendSetter(ClassWriter cw, String className, String fieldName, FieldProperties fieldProperties,
            String methodName, MethodProperties methodProperties)
    {
        if (methodProperties == null || !("(" + fieldProperties.desc + ")V").equals(methodProperties.desc))
        {
            // typified
            appendSetter(cw, className, fieldName, fieldProperties.desc, methodName, fieldProperties.isStatic);

            if (fieldProperties.isEnum)
            {
                // String support
                final MethodVisitor mv = cw.visitMethod(fieldProperties.isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC,
                        methodName, "(" + "Ljava/lang/String;" + ")V", null, null);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitLdcInsn(Type.getType(fieldProperties.desc));
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKESTATIC, "org/alkemy/parse/impl/AlkemizerUtils$Proxy", "toEnum",
                        "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
                mv.visitTypeInsn(CHECKCAST, AlkemizerUtils.toClassNameFromDesc(fieldProperties.desc));
                mv.visitFieldInsn(fieldProperties.isStatic ? PUTSTATIC : PUTFIELD, className, fieldName, fieldProperties.desc);
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            else if (!"Ljava/lang/String;".equals(fieldProperties.desc))
            {
                // String support
                final MethodVisitor mv = cw.visitMethod(fieldProperties.isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC,
                        methodName, "(" + "Ljava/lang/String;" + ")V", null, null);

                mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("Invalid conversion.");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V",
                        false);
                mv.visitInsn(ATHROW);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }

    private static void appendSetter(ClassWriter cw, String className, String name, String desc, String methodName,
            boolean isStatic)
    {
        final MethodVisitor mv = cw.visitMethod(isStatic ? //
        ACC_PUBLIC + ACC_STATIC
                : ACC_PUBLIC, methodName, "(" + desc + ")V", null, null);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(Type.getType(desc).getOpcode(ILOAD), 1);

        mv.visitFieldInsn(isStatic ? PUTSTATIC : PUTFIELD, className, name, desc);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
