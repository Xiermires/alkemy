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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.impl.AbstractReflectionBasedValueAccessor.MemberFieldReflectionBasedAccessor;
import org.alkemy.parse.impl.AbstractReflectionBasedValueAccessor.StaticFieldReflectionBasedAccessor;

class AccessorFactory
{
    private AccessorFactory()
    {
    }

    static ValueAccessor createSelfAccessor(Class<?> type)
    {
        return new SelfAccessor(type);
    }

    static NodeConstructor notSupported()
    {
        return new UnsupportedConstructor();
    }

    static ValueAccessor createAccessor(Field f)
    {
        try
        {
            if (LambdaRefHelper.isInstrumented(f.getDeclaringClass(), null))
            {
                return MethodHandleFactory.createAccessor(f);
            }
            else
            {
                return Modifier.isStatic(f.getModifiers()) ? new StaticFieldReflectionBasedAccessor(f)
                        : new MemberFieldReflectionBasedAccessor(f);
            }
        }
        catch (IllegalAccessException | SecurityException e)
        {
            throw new AlkemyException("Unable to create value accessor for field '%s'", e, f.getDeclaringClass() + "."
                    + f.getName());
        }
    }

    static NodeConstructor createConstructor(Class<?> type, Class<?> componentType)
    {
        type = InterfaceDefaultInstance.get(type);
        componentType = InterfaceDefaultInstance.get(componentType);

        try
        {
            if (LambdaRefHelper.isInstrumented(type, componentType))
            {
                return MethodHandleFactory.createNodeConstructor(type, componentType);
            }
            else
            {
                return new ReflectionBasedConstructorAccessor(type, componentType);
            }
        }
        catch (IllegalAccessException | SecurityException | NoSuchMethodException e)
        {
            throw new AlkemyException("Unable to create a constructor accessor for type '%s'", e, type.getName());
        }
    }

    public static List<MethodInvoker> createInvokers(List<Method> ms)
    {
        final List<MethodInvoker> invokers = new ArrayList<MethodInvoker>();
        for (Method m : ms)
        {
            try
            {
                invokers.add(new MethodHandleBasedInvoker(m, m.getName(), m.getDeclaringClass(), LambdaRefHelper.methodHandle(m)));
            }
            catch (IllegalAccessException e)
            {
                throw new AlkemyException("Unable to create a method invoker for '%s'", e, m.getDeclaringClass() + "."
                        + m.getName());
            }
        }
        return invokers;
    }

    static class SelfAccessor implements ValueAccessor
    {
        Object ref;
        Class<?> type;
        boolean collection;

        SelfAccessor(Class<?> type)
        {
            this.type = type;
            this.collection = Collections.class.isAssignableFrom(type);
        }

        @Override
        public Class<?> type() throws AlkemyException
        {
            return type;
        }

        @Override
        public Object get(Object unused) throws AccessException
        {
            return ref;
        }

        @Override
        public void set(Object value, Object unused) throws AccessException
        {
            ref = value;
        }

        @Override
        public String targetName()
        {
            return type.getTypeName();
        }

        @Override
        public boolean isCollection()
        {
            return collection;
        }
    }

    static class UnsupportedConstructor implements NodeConstructor
    {
        @Override
        public Class<?> type() throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public Object newInstance(Object... args) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public <T> T newInstance(Class<T> type, Object... args) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public Class<?> componentType() throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public Object newComponentInstance(Object... args) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public <T> T newComponentInstance(Class<T> type, Object... args) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }
    }
}
