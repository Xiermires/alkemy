package org.alkemy.parse.impl;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;
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

    static boolean isInstrumented(Class<?> clazz) throws IllegalAccessException, SecurityException
    {
        final Supplier<Boolean> ref = ref2StaticGetter(methodHandle(clazz, Alkemizer.IS_INSTRUMENTED), clazz, boolean.class);
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
                return createLambdaRef(MethodHandles.lookup(), Function.class, Function.class.getMethod("apply", Object.class),
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

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> Supplier<R> ref2StaticGetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException,
            SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(MethodHandles.lookup(), Supplier.class, Supplier.class.getMethod("get"), handle);
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

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> BiConsumer<T, R> ref2MemberSetter(MethodHandle handle, Class<T> clazz, Class<R> r)
            throws IllegalAccessException, SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(MethodHandles.lookup(), BiConsumer.class,
                        BiConsumer.class.getMethod("accept", Object.class, Object.class), handle);
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

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> Consumer<R> ref2StaticSetter(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException,
            SecurityException
    {
        if (Objects.nonNull(handle))
        {
            try
            {
                return createLambdaRef(MethodHandles.lookup(), Consumer.class, Consumer.class.getMethod("accept", Object.class),
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

    static <T> T createLambdaRef(Lookup lookup, Class<T> funcClass, Method funcMethod, MethodHandle handle) throws Throwable
    {
        final Class<?> funcRet = funcMethod.getReturnType();
        final Class<?>[] funcParams = funcMethod.getParameterTypes();
        final MethodType funcType = MethodType.methodType(funcRet, funcParams);

        final CallSite metafactory = LambdaMetafactory.metafactory(MethodHandles.lookup(), funcMethod.getName(),
                MethodType.methodType(funcClass), funcType, handle, handle.type());
        return (T) metafactory.getTarget().invoke();
    }

    static MethodHandle methodHandle(Class<?> clazz, String name, Class<?>... params) throws IllegalAccessException,
            SecurityException
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
