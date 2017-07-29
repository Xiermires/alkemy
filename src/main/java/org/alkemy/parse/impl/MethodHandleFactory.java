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
import java.lang.invoke.MethodHandles;
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

import org.alkemy.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodHandleFactory
{
    private static final Logger log = LoggerFactory.getLogger(MethodHandleFactory.class);

    @SuppressWarnings("unchecked")
    // This method provides accessors for any types, except primitives. Uses functions with obj.
    // references only.
    static ValueAccessor createAccessor(Field f) throws IllegalAccessException, SecurityException
    {
        final ValueAccessor mha;
        final Class<?> clazz = f.getDeclaringClass();

        // Enum setters are instrumented set$$enum_name(Object o) { ... } (the instrumented code
        // allows String->Enum conversion).
        final Class<?> useObjIfEnum = f.getType().isEnum() ? Object.class : f.getType();
        if (Modifier.isStatic(f.getModifiers()))
        {
            final Supplier<?> getter = ref2StaticGetter(methodHandle(clazz, FieldAccessorWriter.getGetterName(f.getName())),
                    clazz, f.getType());
            final Consumer<Object> setter = (Consumer<Object>) ref2StaticSetter(methodHandle(clazz, FieldAccessorWriter
                    .getSetterName(f.getName()), useObjIfEnum), clazz, useObjIfEnum);

            mha = new StaticFieldLambdaBasedAccessor(f.getDeclaringClass().getTypeName() + "." + f.getName(), f.getType(),
                    getter, setter);
        }
        else
        {
            final Function<Object, ?> getter = (Function<Object, ?>) ref2MemberGetter(methodHandle(clazz, FieldAccessorWriter
                    .getGetterName(f.getName())), clazz, f.getType());
            final BiConsumer<Object, Object> setter = (BiConsumer<Object, Object>) ref2MemberSetter(methodHandle(clazz,
                    FieldAccessorWriter.getSetterName(f.getName()), useObjIfEnum), clazz, useObjIfEnum);

            mha = new MemberFieldLambdaBasedAccessor(f.getDeclaringClass().getTypeName() + "." + f.getName(), f.getType(),
                    getter, setter);
        }
        return mha;
    }

    static NodeConstructor createNodeConstructor(Class<?> clazz, Class<?> componentTypeClass) throws IllegalAccessException,
            SecurityException
    {
        final Class<?> instrumentedClass;
        if (componentTypeClass == null)
            instrumentedClass = clazz;
        else instrumentedClass = componentTypeClass;

        final List<Method> l = Arrays.asList(instrumentedClass.getMethods()).stream().filter(
                p -> FieldOrderWriter.CREATE_INSTANCE.equals(p.getName())).collect(Collectors.toList());

        // If an alkemizable class extends another alkemizable class, it receives two static
        // methods. Get the explicit one of this type.
        Method factory = null;
        for (Method m : l)
        {
            if (m.getReturnType().equals(instrumentedClass))
            {
                factory = m;
            }
        }

        Assertions.nonNull(factory);
        final MethodHandle mh = methodHandle(instrumentedClass, FieldOrderWriter.CREATE_INSTANCE, factory.getParameterTypes());

        try
        {
            if (componentTypeClass != null)
            {
                final MethodHandle ctorClass = MethodHandles.lookup().unreflectConstructor(clazz.getConstructor());
                final MethodHandle ctorComponentClass = MethodHandles.lookup().unreflectConstructor(
                        componentTypeClass.getConstructor());
                final Method staticFactory = NodeConstructorFunction.class.getMethod("newInstance", Object[].class);

                return new StaticMethodLambdaBasedConstructor(clazz, componentTypeClass,
                        ref2StaticGetter(ctorClass, clazz, clazz), ref2StaticGetter(ctorComponentClass, componentTypeClass,
                                componentTypeClass), createLambdaRef(NodeConstructorFunction.class, staticFactory, mh));
            }
            else
            {
                final MethodHandle ctorClass = MethodHandles.lookup().unreflectConstructor(clazz.getConstructor());
                final Method staticFactory = NodeConstructorFunction.class.getMethod("newInstance", Object[].class);

                return new StaticMethodLambdaBasedConstructor(clazz, null, ref2StaticGetter(ctorClass, clazz, clazz), null,
                        createLambdaRef(NodeConstructorFunction.class, staticFactory, mh));
            }
        }
        catch (NoSuchMethodException e)
        {
            log.debug("Method not found. Can't use lambdas.", e);
        }
        return null;
    }
}
