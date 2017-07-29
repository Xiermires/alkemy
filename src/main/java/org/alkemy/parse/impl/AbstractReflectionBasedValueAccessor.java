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

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.exception.TypeMismatch;
import org.alkemy.util.AlkemyUtils;
import org.alkemy.util.NumberConversion;

/**
 * This implementation uses reflection to access and modify fields.
 */
public abstract class AbstractReflectionBasedValueAccessor implements ValueAccessor
{
    protected final Field f;
    private final boolean isEnum;
    private final int rank;

    public AbstractReflectionBasedValueAccessor(Field f)
    {
        this.f = f;
        this.isEnum = f.getType().isEnum();
        this.rank = NumberConversion.getRank(f.getType());

        f.setAccessible(true);
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        try
        {
            return f.get(parent);
        }
        catch (final IllegalArgumentException | IllegalAccessException e)
        {
            throw new TypeMismatch("Can't get value from parent type '%s' for target '%s' of type '%s'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public void set(Object value, Object parent) throws AccessException
    {
        try
        {
            if (isEnum)
            {
                AlkemyUtils.setEnum(f, value, parent);
            }
            else
            {
                f.set(parent, rank != -1 ? NumberConversion.convert(value, rank) : value);
            }
        }
        catch (final IllegalArgumentException | IllegalAccessException e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, value != null ? value.getClass().getName() : "null");
        }
    }

    @Override
    public String targetName()
    {
        return f.getDeclaringClass().getName() + "." + f.getName();
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return f.getType();
    }

    public static class MemberFieldReflectionBasedAccessor extends AbstractReflectionBasedValueAccessor
    {
        public MemberFieldReflectionBasedAccessor(Field field)
        {
            super(field);
        }

        @Override
        public Object get(Object parent)
        {
            return parent != null ? super.get(parent) : null;
        }

        @Override
        public <T> T get(Object parent, Class<T> type) throws AlkemyException
        {
            return parent != null ? super.get(parent, type) : null;
        }

        @Override
        public void set(Object value, Object parent) throws AccessException
        {
            if (parent != null)
            {
                super.set(value, parent);
            }
        }
    }

    public static class StaticFieldReflectionBasedAccessor extends AbstractReflectionBasedValueAccessor
    {
        public StaticFieldReflectionBasedAccessor(Field field)
        {
            super(field);
        }
    }
}
