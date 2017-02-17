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

import org.alkemy.ValueAccessor;
import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;

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
                return null;
            }
        }
        catch (IllegalAccessException | SecurityException e)
        {
            // TODO
            throw new RuntimeException("TODO");
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
                return null;
            }
        }
        catch (IllegalAccessException | SecurityException e)
        {
            // TODO
            throw new RuntimeException("TODO");
        }
    }

    static class SelfAccessor implements ValueAccessor
    {
        Object ref;

        @Override
        public Class<?> type() throws AlkemyException
        {
            return ref.getClass();
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
            throw new AlkemyException(""); // TODO (??)
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
        public <T> T newInstance(Object... args) throws AlkemyException
        {
            throw new UnsupportedOperationException("Not supported for this type of element.");
        }
    }
}
