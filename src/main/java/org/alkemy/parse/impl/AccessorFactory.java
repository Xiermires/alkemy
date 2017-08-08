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

import static org.alkemy.parse.impl.MethodReferenceFactory.createReferencedNodeFactory;
import static org.alkemy.parse.impl.MethodReferenceFactory.createReferencedValueAccessor;
import static org.alkemy.parse.impl.MethodReferenceFactory.createReflectedValueAccessor;
import static org.alkemy.parse.impl.MethodReferenceFactory.isInstrumented;
import static org.alkemy.parse.impl.MethodReferenceFactory.methodHandle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.MethodInvoker;
import org.alkemy.parse.NodeFactory;
import org.alkemy.parse.ValueAccessor;

import com.google.common.base.MoreObjects;

class AccessorFactory
{
    private AccessorFactory()
    {
    }

    static ValueAccessor createSelfAccessor(Class<?> type)
    {
        return new SelfAccessor(type);
    }

    static NodeFactory notSupported()
    {
        return new UnsupportedConstructor();
    }

    static ValueAccessor createValueAccessor(Field f)
    {
        try
        {
            if (isInstrumented(f.getDeclaringClass()))
            {
                return createReferencedValueAccessor(f);
            }
            else
            {
                return createReflectedValueAccessor(f);
            }
        }
        catch (ReflectiveOperationException | SecurityException e)
        {
            throw new AlkemyException("Unable to create value accessor for field '%s'", e, f.getDeclaringClass() + "."
                    + f.getName());
        }
    }

    static NodeFactory createNodeFactory(ValueAccessor valueAccessor)
    {
        try
        {
            if (isInstrumented(MoreObjects.firstNonNull(valueAccessor.componentType(), valueAccessor.type())))
            {
                return createReferencedNodeFactory(valueAccessor);
            }
            else
            {
                return new ReflectedNodeFactory(valueAccessor);
            }
        }
        catch (IllegalAccessException | SecurityException e)
        {
            throw new AlkemyException("Unable to create a constructor accessor for type '%s'", e, valueAccessor.type().getName());
        }
    }

    public static List<MethodInvoker> createInvokers(List<Method> ms)
    {
        final List<MethodInvoker> invokers = new ArrayList<MethodInvoker>();
        for (Method m : ms)
        {
            try
            {
                invokers.add(new ReflectedMethodInvoker(m, m.getName(), m.getDeclaringClass(), methodHandle(m)));
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
        public boolean isCollection()
        {
            return collection;
        }

        @Override
        public Class<?> componentType()
        {
            return null;
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
        public String valueName()
        {
            return type.getTypeName();
        }
    }

    static class UnsupportedConstructor implements NodeFactory
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
        public <E> E newInstance(Class<E> type, Object... args) throws AlkemyException
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
        public <E> E newComponentInstance(Class<E> type, Object... args) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public Object get(Object parent) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public void set(Object value, Object parent) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public String valueName()
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }

        @Override
        public boolean isCollection()
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }
    }
}
