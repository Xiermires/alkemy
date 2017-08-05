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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodReferenceHelper
{
    private static final Logger log = LoggerFactory.getLogger(MethodReferenceHelper.class);

    private MethodReferenceHelper()
    {
    }

    static <T> T methodReference(Class<T> funcClass, Method reference, MethodHandle referent)
    {
        try
        {
            final Class<?> funcRet = reference.getReturnType();
            final Class<?>[] funcParams = reference.getParameterTypes();
            final MethodType funcType = MethodType.methodType(funcRet, funcParams);

            final CallSite metafactory = LambdaMetafactory.metafactory(MethodHandles.lookup(), reference.getName(), MethodType
                    .methodType(funcClass), funcType, referent, referent.type());
            return (T) metafactory.getTarget().invoke();
        }
        catch (Throwable e)
        {
            log.debug(String.format("Can't create lambda reference for type '%s'.", funcClass.getName()), e);
        }
        return null;
    }

    static MethodHandle methodHandle(Class<?> clazz, String name, Class<?>... params) throws IllegalAccessException,
            SecurityException, NoSuchMethodException
    {
        return MethodHandles.lookup().unreflect(clazz.getDeclaredMethod(name, params));
    }

    static MethodHandle methodHandle(Method m) throws IllegalAccessException
    {
        return MethodHandles.lookup().unreflect(m);
    }
}
