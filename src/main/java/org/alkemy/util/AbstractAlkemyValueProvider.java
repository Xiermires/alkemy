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
package org.alkemy.util;

import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.visitor.AlkemyValueProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public abstract class AbstractAlkemyValueProvider<E extends AbstractAlkemyElement<E>, P> implements AlkemyValueProvider<E, P>
{
    // enums are slower.
    static final int DOUBLE = 0;
    static final int FLOAT = 1;
    static final int LONG = 2;
    static final int INTEGER = 3;
    static final int SHORT = 4;
    static final int BYTE = 5;
    static final int CHAR = 6;
    static final int BOOLEAN = 7;
    static final int OBJECT = 8;
    
    static final ImmutableMap<Class<?>, Integer> types;
    static 
    {
        final Builder<Class<?>, Integer> b = ImmutableMap.builder();
        
        b.put(Double.class, DOUBLE);
        b.put(double.class, DOUBLE);
        b.put(Float.class, FLOAT);
        b.put(float.class, FLOAT);
        b.put(Long.class, LONG);
        b.put(long.class, LONG);
        b.put(Integer.class, INTEGER);
        b.put(int.class, INTEGER);
        b.put(Short.class, SHORT);
        b.put(short.class, SHORT);
        b.put(Byte.class, BYTE);
        b.put(byte.class, BYTE);
        b.put(Character.class, CHAR);
        b.put(char.class, CHAR);
        b.put(Boolean.class, BOOLEAN);
        b.put(boolean.class, BOOLEAN);
        
        types = b.build();
    }
    
    public int type(AbstractAlkemyElement<?> e)
    {
        // handle primitives && wrappers
        final Integer type = types.get(e.type());
        return type == null ? OBJECT : type.intValue();
    }

    @Override
    public Object getValue(E e, P p)
    {
        switch (type(e))
        {
            case 0:
                return getDouble(e, p);
            case 1:
                return getFloat(e, p);
            case 2:
                return getLong(e, p);
            case 3:
                return getInteger(e, p);
            case 4:
                return getShort(e, p);
            case 5:
                return getByte(e, p);
            case 6:
                return getChar(e, p);
            case 7:
                return getBoolean(e, p);
            case 8:
                return getObject(e, p);
            default:
                throw new AlkemyException("Undefined type '%d'", e.type());
        }
    }

    @Override
    public Double getDouble(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Float getFloat(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Long getLong(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Integer getInteger(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Short getShort(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Byte getByte(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Character getChar(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Boolean getBoolean(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Object getObject(E e, P p)
    {
        throw new UnsupportedOperationException("not implemented.");
    }
}
