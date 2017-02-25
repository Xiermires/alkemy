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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;

public class MethodHandleBasedInvoker implements MethodInvoker
{
    private final AnnotatedElement desc;
    private final String name;
    private final Class<?> declaringClass;
    private final MethodHandle mh;
    private final boolean hasArgs;

    MethodHandleBasedInvoker(AnnotatedElement desc, String name, Class<?> declaringClass, MethodHandle mh)
    {
        this.desc = desc;
        this.name = name;
        this.declaringClass = declaringClass;
        this.mh = mh;
        this.hasArgs = mh.type().parameterCount() > 1; // 1: class instance
    }

    @Override
    public AnnotatedElement desc()
    {
        return desc;
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public Class<?> declaringClass()
    {
        return declaringClass;
    }

    @Override
    public Optional<Object> invoke(Object parent, Object... args) throws AlkemyException
    {
        try
        {
            if (hasArgs)
            {
                final Object[] os = new Object[args.length + 1];
                os[0] = parent;
                System.arraycopy(args, 0, os, 1, args.length);
                return Optional.ofNullable(mh.invokeWithArguments(os));
            }
            else
            {
                return Optional.ofNullable(mh.invoke(parent));
            }
        }
        catch (Throwable e)
        {
            throw new AccessException("Method invokation error for method '%s' using type '%s' and arguments '%s'", e,
                    declaringClass.getName() + "." + name(), parent, Arrays.asList(args));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // safe
    public <R> R safeInvoke(Object parent, Class<R> retType, Object... args) throws AlkemyException
    {
        final Optional<Object> o = invoke(parent, args);
        if (o.isPresent())
        {
            final Object v = o.get();
            return retType == v.getClass() ? (R) v : null;
        }
        return null;
    }
}
