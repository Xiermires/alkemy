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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Supplier;

import org.alkemy.instr.ConstructorWriter;
import org.alkemy.instr.DefaultAlkemizerWriter;
import org.alkemy.parse.AutoCastValueAccessor;
import org.alkemy.parse.ConstructorFunction;
import org.alkemy.parse.InterfaceDefaultInstance;
import org.alkemy.parse.NodeFactory;
import org.alkemy.parse.ValueAccessor;
import org.alkemy.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;

@SuppressWarnings("unchecked")
public class MethodReferenceFactory
{
    private static final Logger log = LoggerFactory.getLogger(MethodReferenceFactory.class);

    static boolean isInstrumented(Class<?> clazz) throws IllegalAccessException, SecurityException
    {
        try
        {
            final Method get = Supplier.class.getMethod("get");
            final MethodHandle referent = methodHandle(clazz, DefaultAlkemizerWriter.IS_INSTRUMENTED);
            final Supplier<Boolean> instrumented = methodReference(Supplier.class, get, referent);
            return instrumented != null && instrumented.get();
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
    }

    static ValueAccessor createReflectedValueAccessor(Field f) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        return Modifier.isStatic(f.getModifiers()) ? new ReflectedStaticValueAccessor(f) : new ReflectedMemberValueAccessor(f);
    }

    static ValueAccessor createReferencedValueAccessor(Field f) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        final ValueAccessor valueAccessor = new StringReference(f);

        if (f.getType() == double.class) return new DoubleReference(f);
        else if (f.getType() == float.class) return new FloatReference(f);
        else if (f.getType() == long.class) return new LongReference(f);
        else if (f.getType() == int.class) return new IntReference(f);
        else if (f.getType() == short.class) return new ShortReference(f);
        else if (f.getType() == char.class) return new CharReference(f);
        else if (f.getType() == byte.class) return new ByteReference(f);
        else if (f.getType() == boolean.class) return new BooleanReference(f);
        else
            return valueAccessor;
    }

    static NodeFactory createReferencedNodeFactory(AutoCastValueAccessor valueAccessor) throws IllegalAccessException, SecurityException
    {
        // If an alkemizable class extends another alkemizable class, it
        // receives two static
        // methods. Get the explicit one of this type.
        Method factoryMethod = null;
        final Class<? extends Object> instrumentedType = MoreObjects.firstNonNull(valueAccessor.componentType(), valueAccessor.type());
        for (Method m : Iterables.filter(Arrays.asList(instrumentedType.getMethods())//
                , f -> ConstructorWriter.CREATE_ARGS.equals(f.getName())))
        {
            if (m.getReturnType().equals(instrumentedType)) factoryMethod = m;
        }

        Assertions.nonNull(factoryMethod);

        MethodHandle factory = null;
        try
        {
            factory = MethodReferenceFactory.methodHandle(instrumentedType, ConstructorWriter.CREATE_ARGS, factoryMethod.getParameterTypes());
        }
        catch (NoSuchMethodException e)
        {
            log.debug("Factory method not present in type '{}'.", valueAccessor.type().getName());
        }

        try
        {
            final Supplier<Object> typeCtor = getTypeCtor(valueAccessor.type());
            final Method factoryReference = ConstructorFunction.class.getMethod("newInstance", Object[].class);
            final ConstructorFunction factoryWithArgs = MethodReferenceFactory.methodReference(ConstructorFunction.class, factoryReference, factory);

            final Supplier<Object> componentTypeCtor;
            if (valueAccessor.componentType() != null)
            {
                componentTypeCtor = getTypeCtor(valueAccessor.componentType());
            }
            else
            {
                componentTypeCtor = null;
            }
            return new ReferencedNodeFactory(typeCtor//
                    , componentTypeCtor//
                    , factoryWithArgs//
                    , valueAccessor);
        }
        catch (NoSuchMethodException e)
        {
            log.debug("Method not found. Can't use lambdas.", e);
        }
        return null;
    }

    static Supplier<Object> getTypeCtor(Class<?> type) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        final Method get = Supplier.class.getMethod("get");

        final Class<?> _type;
        if (type.isInterface())
        {
            _type = InterfaceDefaultInstance.get(type);
        }
        else
            _type = type;

        MethodHandle ctorHandle;
        try
        {
            ctorHandle = MethodHandles.lookup().unreflectConstructor(_type.getConstructor());
        }
        catch (NoSuchMethodException e)
        {
            ctorHandle = MethodHandles.lookup().unreflect(_type.getDeclaredMethod(ConstructorWriter.CREATE_DEFAULT));
        }
        return MethodReferenceFactory.methodReference(Supplier.class, get, ctorHandle);
    }

    static <T> T methodReference(Class<T> funcClass, Method reference, MethodHandle referent)
    {
        try
        {
            final Class<?> funcRet = reference.getReturnType();
            final Class<?>[] funcParams = reference.getParameterTypes();
            final MethodType funcType = MethodType.methodType(funcRet, funcParams);

            final CallSite metafactory = LambdaMetafactory.metafactory(MethodHandles.lookup(), reference.getName(), MethodType.methodType(funcClass), funcType, referent, referent.type());
            return (T) metafactory.getTarget().invoke();
        }
        catch (Throwable e)
        {
            log.debug(String.format("Can't create lambda reference for type '%s'.", funcClass.getName()), e);
        }
        return null;
    }

    static MethodHandle methodHandle(Class<?> clazz, String name, Class<?>... params) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        return MethodHandles.lookup().unreflect(clazz.getDeclaredMethod(name, params));
    }

    static MethodHandle methodHandle(Method m) throws IllegalAccessException
    {
        return MethodHandles.lookup().unreflect(m);
    }
}
