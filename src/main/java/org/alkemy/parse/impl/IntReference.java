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
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

import org.alkemy.exception.AlkemyException;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class IntReference extends LambdaReference implements ValueAccessor
{
    private final ToIntFunction<Object> igetter;
    private final ObjIntConsumer<Object> isetter;

    @SuppressWarnings("unchecked")
    public IntReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);

        if (Modifier.isStatic(f.getModifiers()))
        {
            isetter = (i, v) -> super.set(v, i);
            igetter = (i) -> super.getInt(i);
        }
        else
        {
            final String setterName = AlkemizerUtils.getSetterName(f.getName());
            final MethodHandle isetterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, int.class);
            final Method iaccept = ObjIntConsumer.class.getMethod("accept", Object.class, int.class);
            isetter = MethodReferenceFactory.methodReference(ObjIntConsumer.class, iaccept, isetterHandle);

            final String getterName = AlkemizerUtils.getGetterName(f.getName());
            final MethodHandle igetterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
            final Method iapply = ToIntFunction.class.getMethod("applyAsInt", Object.class);
            igetter = MethodReferenceFactory.methodReference(ToIntFunction.class, iapply, igetterHandle);
        }
    }

    public int getInt(Object parent)
    {
        return igetter.applyAsInt(parent);
    }

    @Override
    public void set(int value, Object parent) throws AlkemyException
    {
        isetter.accept(parent, value);
    }
}
