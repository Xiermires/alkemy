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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.alkemy.exception.AlkemyException;
import org.alkemy.instr.AlkemizerUtils;

import com.google.common.base.Supplier;

public class LambdaReference extends Reference
{
    // All types.
    private final Function<Object, ?> getter;
    private final BiConsumer<Object, Object> setter;

    @SuppressWarnings("unchecked")
    LambdaReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        
        final Class<?> clazz = f.getDeclaringClass();
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(clazz, AlkemizerUtils.getGetterName(f.getName()));
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(clazz, AlkemizerUtils.getSetterName(f.getName()), f.getType());

        if (Modifier.isStatic(f.getModifiers()))
        {
            final Method get = Supplier.class.getMethod("get");
            final Method accept = Consumer.class.getMethod("accept", Object.class);

            final Supplier<Object> getter = MethodReferenceFactory.methodReference(Supplier.class, get, getterHandle);
            final Consumer<Object> setter = MethodReferenceFactory.methodReference(Consumer.class, accept, setterHandle);
            
            this.getter = (i) -> getter.get();
            this.setter = (i, v) -> setter.accept(v);
        }
        else
        {
            final Method apply = Function.class.getMethod("apply", Object.class);
            final Method accept = BiConsumer.class.getMethod("accept", Object.class, Object.class);

            getter = MethodReferenceFactory.methodReference(Function.class, apply, getterHandle);
            setter = MethodReferenceFactory.methodReference(BiConsumer.class, accept, setterHandle);
        }
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        return getter.apply(parent);
    }

    @Override
    public void set(Object value, Object parent) throws AlkemyException
    {
        setter.accept(parent, value);
    }
}
