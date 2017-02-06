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
package org.alkemy.methodhandle;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.alkemy.alkemizer.Alkemizer;
import org.alkemy.core.ValueAccessor;
import org.apache.log4j.Logger;

public class MethodHandleAccessorFactory
{
    private static final Logger log = Logger.getLogger(MethodHandleAccessorFactory.class);

    public static boolean isInstrumented(Field f) throws IllegalAccessException, SecurityException
    {
        final Class<?> clazz = f.getDeclaringClass();
        final Supplier<Boolean> ref = ref2StaticGetter(methodHandle(clazz, Alkemizer.IS_INSTRUMENTED), clazz, boolean.class);
        return Objects.nonNull(ref) && ref.get();
    }

    @SuppressWarnings("unchecked") 
    // This method provides accessors for any types, except primitives. Uses functions with obj. references only.
    public static ValueAccessor createAccessor(Field f) throws IllegalAccessException, SecurityException
    {
        final ValueAccessor mha;
        final Class<?> clazz = f.getDeclaringClass(); // TODO: Check inner classes.

        if (Modifier.isStatic(f.getModifiers()))
        {
            final Supplier<?> getter = ref2StaticGetter(methodHandle(clazz, Alkemizer.getGetterName(f.getName())), clazz, f.getType());
            final Consumer<Object> setter = (Consumer<Object>) ref2StaticSetter(methodHandle(clazz, Alkemizer.getSetterName(f.getName()), f.getType()), clazz, f.getType());

            mha = new StaticMethodHandleAccessor(clazz, f.getName(), f.getType(), getter, setter);
        }
        else
        {
            final Function<Object, ?> getter = (Function<Object, ?>) ref2MemberGetter(methodHandle(clazz, Alkemizer.getGetterName(f.getName())), clazz, f.getType());
            final BiConsumer<Object, Object> setter = (BiConsumer<Object, Object>) ref2MemberSetter(methodHandle(clazz, Alkemizer.getSetterName(f.getName()), f.getType()), clazz, f.getType());

            mha = new MemberMethodHandleAccessor(clazz, f.getName(), f.getType(), getter, setter);
        }
        return mha;
    }

    @SuppressWarnings("unchecked") // safe
    public static <T, R> Function<T, R> ref2MemberGetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException, SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(MethodHandles.lookup(), Function.class, Function.class.getMethod("apply", Object.class), handle);
            }
            catch (IllegalAccessException | SecurityException e)
            {
                throw e;
            }
            catch (Throwable e)
            {
                log.debug(String.format("Can't create lambda reference for type '%s'.", clazz.getName()), e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked") // safe
    public static <T, R> Supplier<R> ref2StaticGetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException, SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return (Supplier<R>) createLambdaRef(MethodHandles.lookup(), Supplier.class, Supplier.class.getMethod("get"), handle);
            }
            catch (IllegalAccessException | SecurityException e)
            {
                throw e;
            }
            catch (Throwable e)
            {
                log.debug(String.format("Can't create lambda reference for type '%s'.", clazz.getName()), e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked") // safe
    public static <T, R> BiConsumer<T, R> ref2MemberSetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException, SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return (BiConsumer<T, R>) createLambdaRef(MethodHandles.lookup(), BiConsumer.class, BiConsumer.class.getMethod("accept", Object.class, Object.class),
                        handle);
            }
            catch (IllegalAccessException | SecurityException e)
            {
                throw e;
            }
            catch (Throwable e)
            {
                log.debug(String.format("Can't create lambda reference for type '%s'.", clazz.getName()), e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked") // safe
    public static <T, R> Consumer<R> ref2StaticSetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException, SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return (Consumer<R>) createLambdaRef(MethodHandles.lookup(), Consumer.class, Consumer.class.getMethod("accept", Object.class), handle);
            }
            catch (IllegalAccessException | SecurityException e)
            {
                throw e;
            }
            catch (Throwable e)
            {
                log.debug(String.format("Can't create lambda reference for type '%s'.", clazz.getName()), e);
            }
        }
        return null;
    }

    private static <T> T createLambdaRef(Lookup lookup, Class<T> funcClass, Method funcMethod, MethodHandle handle) throws Throwable
    {
        final Class<?> funcRet = funcMethod.getReturnType();
        final Class<?>[] funcParams = funcMethod.getParameterTypes();
        final MethodType funcType = MethodType.methodType(funcRet, funcParams);

        final CallSite metafactory = LambdaMetafactory.metafactory(MethodHandles.lookup(), funcMethod.getName(), MethodType.methodType(funcClass), funcType, handle,
                handle.type());
        return (T) metafactory.getTarget().invoke();
    }

    public static MethodHandle methodHandle(Class<?> clazz, String name, Class<?>... params) throws IllegalAccessException, SecurityException
    {
        try
        {
            return MethodHandles.lookup().unreflect(clazz.getMethod(name, params));
        }
        catch (NoSuchMethodException e)
        {
            log.debug(String.format("Type '%s' not instrumented, or error in the instrumentation.", clazz.getName()), e);
        }
        return null;
    }
}
