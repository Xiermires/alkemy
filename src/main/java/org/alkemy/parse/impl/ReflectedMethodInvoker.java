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
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.MethodInvoker;

public class ReflectedMethodInvoker implements MethodInvoker
{
    private final AnnotatedElement desc;
    private final String name;
    private final Class<?> declaringClass;
    private final MethodHandle mh;
    private final boolean hasArgs;

    ReflectedMethodInvoker(AnnotatedElement desc, String name, Class<?> declaringClass, MethodHandle mh)
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
    public <R> R invoke(Object parent, Class<R> retType, Object... args) throws AlkemyException
    {
        final Optional<Object> o = invoke(parent, args);
        if (o.isPresent())
        {
            final Object v = o.get();
            return v != null && retType.isInstance(v) ? (R) v : null;
        }
        return null;
    }
}
