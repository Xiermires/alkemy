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
import org.alkemy.functional.ObjShortConsumer;
import org.alkemy.functional.ToShortFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class ShortReference extends LambdaReference implements ValueAccessor
{
    private final ToShortFunction<Object> sgetter;
    private final ObjShortConsumer<Object> ssetter;

    @SuppressWarnings("unchecked")
    public ShortReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);

        if (Modifier.isStatic(f.getModifiers()))
        {
            ssetter = (i, v) -> super.set(v, i);
            sgetter = (i) -> super.getShort(i);
        }
        else
        {
            final String setterName = AlkemizerUtils.getSetterName(f.getName());
            final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, short.class);
            final Method accept = ObjShortConsumer.class.getMethod("accept", Object.class, short.class);
            ssetter = MethodReferenceFactory.methodReference(ObjShortConsumer.class, accept, setterHandle);

            final String getterName = AlkemizerUtils.getGetterName(f.getName());
            final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
            final Method apply = ToShortFunction.class.getMethod("apply", Object.class);
            sgetter = MethodReferenceFactory.methodReference(ToShortFunction.class, apply, getterHandle);
        }
    }

    public short getShort(Object parent)
    {
        return sgetter.apply(parent);
    }

    @Override
    public void set(short value, Object parent) throws AlkemyException
    {
        ssetter.accept(parent, value);
    }
}
