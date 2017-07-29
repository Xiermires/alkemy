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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaRefHelper
{
    private static final Logger log = LoggerFactory.getLogger(LambdaRefHelper.class);

    private LambdaRefHelper()
    {
    }

    static boolean isInstrumented(Class<?> clazz, Class<?> componentTypeClass) throws IllegalAccessException, SecurityException
    {
        final Supplier<Boolean> ref;
        if (componentTypeClass != null)
            ref = ref2StaticGetter(methodHandle(componentTypeClass, FieldAlkemizer.IS_INSTRUMENTED), componentTypeClass, boolean.class);
        else
            ref = ref2StaticGetter(methodHandle(clazz, FieldAlkemizer.IS_INSTRUMENTED), clazz, boolean.class);
        
        return Objects.nonNull(ref) && ref.get();
    }

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> Function<T, R> ref2MemberGetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException,
            SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(Function.class, Function.class.getMethod("apply", Object.class), handle);
            }
            catch (SecurityException e)
            {
                throw e;
            }
            catch (NoSuchMethodException e)
            {
                log.debug("Functional interface doesn't implement", e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> Supplier<R> ref2StaticGetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException,
            SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(Supplier.class, Supplier.class.getMethod("get"), handle);
            }
            catch (SecurityException e)
            {
                throw e;
            }
            catch (NoSuchMethodException e)
            {
                log.debug("Functional interface doesn't implement", e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> BiConsumer<T, R> ref2MemberSetter(MethodHandle handle, Class<T> clazz, Class<R> r)
            throws IllegalAccessException, SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(BiConsumer.class, BiConsumer.class.getMethod("accept", Object.class, Object.class), handle);
            }
            catch (SecurityException e)
            {
                throw e;
            }
            catch (NoSuchMethodException e)
            {
                log.debug("Functional interface doesn't implement", e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> Consumer<R> ref2StaticSetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException,
            SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(Consumer.class, Consumer.class.getMethod("accept", Object.class), handle);
            }
            catch (SecurityException e)
            {
                throw e;
            }
            catch (NoSuchMethodException e)
            {
                log.debug("Functional interface doesn't implement", e);
            }
        }
        return null;
    }

    static <T> T createLambdaRef(Class<T> funcClass, Method funcMethod, MethodHandle handle)
    {
        try
        {
            final Class<?> funcRet = funcMethod.getReturnType();
            final Class<?>[] funcParams = funcMethod.getParameterTypes();
            final MethodType funcType = MethodType.methodType(funcRet, funcParams);

            final CallSite metafactory = LambdaMetafactory.metafactory(MethodHandles.lookup(), funcMethod.getName(),
                    MethodType.methodType(funcClass), funcType, handle, handle.type());
            return (T) metafactory.getTarget().invoke();
        }
        catch (Throwable e)
        {
            log.debug(String.format("Can't create lambda reference for type '%s'.", funcClass.getName()), e);
        }
        return null;
    }

    /*
     * Maintain weak references of the failed classes to log once per class and tree generation at
     * most.
     * <p>
     * Assumption is that they hold enough so that the whole reflection fallbacks are generated and
     * we get the log message only once.
     * <p>
     * This is good enough to avoid log flood in most situations. 
     */
    static final WeakHashMap<Class<?>, Object> failed = new WeakHashMap<>();

    static MethodHandle methodHandle(Class<?> clazz, String name, Class<?>... params) throws IllegalAccessException,
            SecurityException
    {
        try
        {
            return failed.containsKey(clazz) ? null : MethodHandles.lookup().unreflect(clazz.getDeclaredMethod(name, params));
        }
        catch (NoSuchMethodException e)
        {
            log.debug(String.format("Type '%s' not instrumented, or error in the instrumentation. Fallback to reflection.",
                    clazz.getName()));
            failed.put(clazz, null);
        }
        return null;
    }

    static MethodHandle methodHandle(Method m) throws IllegalAccessException 
    {
        return MethodHandles.lookup().unreflect(m);
    }
}
