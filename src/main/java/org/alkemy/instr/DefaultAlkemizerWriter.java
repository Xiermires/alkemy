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
package org.alkemy.instr;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IRETURN;

import java.util.List;
import java.util.Map;

import org.alkemy.annotations.Order;
import org.alkemy.instr.DefaultAlkemizableVisitor.FieldProperties;
import org.alkemy.instr.DefaultAlkemizableVisitor.MethodProperties;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class DefaultAlkemizerWriter extends ClassVisitor
{
    public static final String IS_INSTRUMENTED = "is$$instrumented";

    private final ClassWriter cw;
    private final String className;
    private final Map<String, FieldProperties> fieldMap;
    private final Map<String, MethodProperties> methodMap;
    private final List<String> orderedFields;

    private boolean hasDefaultCtor;
    private boolean ordered = false;

    public DefaultAlkemizerWriter(ClassWriter cw, String className, List<String> orderedFields,
            Map<String, FieldProperties> fieldMap, Map<String, MethodProperties> methodMap)
    {
        super(ASM5, cw);
        this.cw = cw;
        this.className = className;
        this.orderedFields = orderedFields;
        this.fieldMap = fieldMap;
        this.methodMap = methodMap;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
    {
        ordered |= Order.class.getName().equals(AlkemizerUtils.toQualifiedNameFromDesc(desc));
        return super.visitAnnotation(desc, visible);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        int _access = access;
        if (AlkemizerUtils.isDefaultCtor(name, desc))
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

    @Override
    public void visitEnd()
    {
        if (!ordered)
            appendOrder();
        
        appendIsInstrumented();
        
        ConstructorWriter.appendCreateDefault(cw, className, hasDefaultCtor);
        ConstructorWriter.appendCreateArgs(cw, className, orderedFields, fieldMap);
        GetterSetterWriter.appendGetters(cw, className, orderedFields, fieldMap, methodMap);
        GetterSetterWriter.appendSetters(cw, className, orderedFields, fieldMap, methodMap);

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

    private void appendOrder()
    {
        final AnnotationVisitor av = super.visitAnnotation("Lorg/alkemy/annotations/Order;", true);
        final AnnotationVisitor aav = av.visitArray("value");
        orderedFields.forEach(name -> aav.visit(null, name));
        aav.visitEnd();
        av.visitEnd();
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
}
