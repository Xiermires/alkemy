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

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ASM5;

import java.util.LinkedHashMap;
import java.util.Map;

import org.alkemy.instr.DefaultAlkemizableVisitor.FieldProperties;
import org.alkemy.instr.DefaultAlkemizableVisitor.MethodProperties;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public abstract class AbstractAlkemizableVisitor extends Alkemizer
{
    protected final String className;
    protected final Map<String, FieldProperties> fieldMap = new LinkedHashMap<>();
    protected final Map<String, MethodProperties> methodMap = new LinkedHashMap<>();

    public AbstractAlkemizableVisitor(String className)
    {
        super(ASM5);
        this.className = className;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
    {
        boolean isStatic = (access & ACC_STATIC) != 0;
        boolean isEnum = AlkemizerUtils.isType(desc) ? AlkemizerUtils.isEnum(desc) : false;

        fieldMap.put(name, new FieldProperties(desc, signature, isEnum, isStatic));
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        boolean isStatic = (access & ACC_STATIC) != 0;
        
        methodMap.put(name, new MethodProperties(desc, signature, isStatic));
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
