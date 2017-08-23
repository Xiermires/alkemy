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
package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjCharConsumer;
import org.alkemy.functional.ToCharFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class CharReference extends LambdaReference implements ValueAccessor
{
    private final ToCharFunction<Object> cgetter;
    private final ObjCharConsumer<Object> csetter;

    @SuppressWarnings("unchecked")
    public CharReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);

        if (Modifier.isStatic(f.getModifiers()))
        {
            csetter = (i, v) -> super.set(v, i);
            cgetter = (i) -> super.getChar(i);
        }
        else
        {
            final String setterName = AlkemizerUtils.getSetterName(f.getName());
            final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, char.class);
            final Method accept = ObjCharConsumer.class.getMethod("accept", Object.class, char.class);
            csetter = MethodReferenceFactory.methodReference(ObjCharConsumer.class, accept, setterHandle);

            final String getterName = AlkemizerUtils.getGetterName(f.getName());
            final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
            final Method apply = ToCharFunction.class.getMethod("apply", Object.class);
            cgetter = MethodReferenceFactory.methodReference(ToCharFunction.class, apply, getterHandle);
        }
    }

    public char getChar(Object parent)
    {
        return cgetter.apply(parent);
    }

    @Override
    public void set(char value, Object parent) throws AlkemyException
    {
        csetter.accept(parent, value);
    }
}
