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

import static org.alkemy.parse.impl.LambdaRefHelper.createLambdaRef;
import static org.alkemy.parse.impl.LambdaRefHelper.methodHandle;
import static org.alkemy.parse.impl.LambdaRefHelper.ref2MemberGetter;
import static org.alkemy.parse.impl.LambdaRefHelper.ref2MemberSetter;
import static org.alkemy.parse.impl.LambdaRefHelper.ref2StaticGetter;
import static org.alkemy.parse.impl.LambdaRefHelper.ref2StaticSetter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.alkemy.ValueAccessor;
import org.alkemy.util.Conditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodHandleFactory
{
    private static final Logger log = LoggerFactory.getLogger(MethodHandleFactory.class);

    @SuppressWarnings("unchecked")
    // This method provides accessors for any types, except primitives. Uses functions with obj. references only.
    static ValueAccessor createAccessor(Field f) throws IllegalAccessException, SecurityException
    {
        final ValueAccessor mha;
        final Class<?> clazz = f.getDeclaringClass(); // TODO: Check inner classes.

        if (Modifier.isStatic(f.getModifiers()))
        {
            final Supplier<?> getter = ref2StaticGetter(methodHandle(clazz, Alkemizer.getGetterName(f.getName())), clazz,
                    f.getType());
            final Consumer<Object> setter = (Consumer<Object>) ref2StaticSetter(
                    methodHandle(clazz, Alkemizer.getSetterName(f.getName()), f.getType()), clazz, f.getType());

            mha = new StaticFieldLambdaBasedAccessor(f.getName(), f.getType(), getter, setter);
        }
        else
        {
            final Function<Object, ?> getter = (Function<Object, ?>) ref2MemberGetter(
                    methodHandle(clazz, Alkemizer.getGetterName(f.getName())), clazz, f.getType());
            final BiConsumer<Object, Object> setter = (BiConsumer<Object, Object>) ref2MemberSetter(
                    methodHandle(clazz, Alkemizer.getSetterName(f.getName()), f.getType()), clazz, f.getType());

            mha = new MemberFieldLambdaBasedAccessor(f.getName(), f.getType(), getter, setter);
        }
        return mha;
    }

    static NodeConstructor createNodeConstructor(Class<?> clazz) throws IllegalAccessException, SecurityException
    {
        final List<Method> l = Arrays.asList(clazz.getMethods()).stream()
                .filter(p -> Alkemizer.CREATE_INSTANCE.equals(p.getName())).collect(Collectors.toList());

        Conditions.requireCollectionSize(l, 1); // we instrument only one factory method

        final MethodHandle mh = methodHandle(clazz, Alkemizer.CREATE_INSTANCE, l.get(0).getParameterTypes());

        Method m;
        try
        {
            m = NodeConstructorFunction.class.getMethod("newInstance", Object[].class);
            return new StaticMethodLambdaBasedConstructor(clazz, createLambdaRef(NodeConstructorFunction.class, m, mh));
        }
        catch (NoSuchMethodException e)
        {
            log.debug("Functional interface doesn't implement", e);
        }
        return null;
    }
}
