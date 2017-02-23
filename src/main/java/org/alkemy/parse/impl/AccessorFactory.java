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
import java.lang.reflect.Modifier;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.impl.AbstractReflectionBasedValueAccessor.MemberFieldReflectionBasedAccessor;
import org.alkemy.parse.impl.AbstractReflectionBasedValueAccessor.StaticFieldReflectionBasedAccessor;

class AccessorFactory
{
    private AccessorFactory()
    {
    }

    static ValueAccessor createSelfAccessor()
    {
        return new SelfAccessor();
    }

    static NodeConstructor notSupported()
    {
        return new UnsupportedConstructor();
    }

    static ValueAccessor createAccessor(Field f)
    {
        try
        {
            if (LambdaRefHelper.isInstrumented(f.getDeclaringClass()))
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
            // TODO (Security)
            throw new RuntimeException("TODO", e);
        }
    }

    static NodeConstructor createConstructor(Class<?> type)
    {
        try
        {
            if (LambdaRefHelper.isInstrumented(type))
            {
                return MethodHandleFactory.createNodeConstructor(type);
            }
            else
            {
                return new ReflectionBasedConstructorAccessor(type.getConstructor());
            }
        }
        catch (IllegalAccessException | SecurityException | NoSuchMethodException e)
        {
            // TODO (Class not public // No default ctor // Security)
            throw new RuntimeException("TODO", e);
        }
    }

    static class SelfAccessor implements ValueAccessor
    {
        Object ref;

        @Override
        public Class<?> type() throws AlkemyException
        {
            return ref != null ? ref.getClass() : null;
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
            return ref != null ? ref.getClass().getTypeName() : null;
        }

        @Override
        @SuppressWarnings("unchecked") // safe
        public <T> T safeGet(Object parent, Class<T> type) throws AlkemyException
        {
            return ref == null || type == ref.getClass() ? (T) ref : null;
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
        public <T> T safeNewInstance(Class<T> type, Object... args) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }
    }
}
