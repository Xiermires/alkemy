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
package org.alkemy;

import org.alkemy.exception.AlkemyException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class AbstractAlkemyProvider implements AlkemyProvider
{
    static final ImmutableMap<Class<?>, Integer> types;
    static 
    {
        final Builder<Class<?>, Integer> b = ImmutableMap.builder();
        
        b.put(Double.class, AlkemyProvider.DOUBLE);
        b.put(double.class, AlkemyProvider.DOUBLE);
        b.put(Float.class, AlkemyProvider.FLOAT);
        b.put(float.class, AlkemyProvider.FLOAT);
        b.put(Long.class, AlkemyProvider.LONG);
        b.put(long.class, AlkemyProvider.LONG);
        b.put(Integer.class, AlkemyProvider.INTEGER);
        b.put(int.class, AlkemyProvider.INTEGER);
        b.put(Short.class, AlkemyProvider.SHORT);
        b.put(short.class, AlkemyProvider.SHORT);
        b.put(Byte.class, AlkemyProvider.BYTE);
        b.put(byte.class, AlkemyProvider.BYTE);
        b.put(Character.class, AlkemyProvider.CHAR);
        b.put(char.class, AlkemyProvider.CHAR);
        b.put(Boolean.class, AlkemyProvider.BOOLEAN);
        b.put(boolean.class, AlkemyProvider.BOOLEAN);
        
        types = b.build();
    }
    
    @Override
    public Key createKey(AbstractAlkemyElement<?> e)
    {
        // handle primitives && wrappers
        final Integer type = types.get(e.type());
        final int typeAsInt = type == null ? AlkemyProvider.OBJECT : type.intValue();
        return () -> typeAsInt;
    }

    @Override
    public Object getValue(Key key)
    {
        switch (key.type())
        {
            case 0:
                return getDouble(key);
            case 1:
                return getFloat(key);
            case 2:
                return getLong(key);
            case 3:
                return getInteger(key);
            case 4:
                return getShort(key);
            case 5:
                return getByte(key);
            case 6:
                return getChar(key);
            case 7:
                return getBoolean(key);
            case 8:
                return getObject(key);
            default:
                throw new AlkemyException("Undefined type '%d'", key.type());
        }
    }

    @Override
    public Double getDouble(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Float getFloat(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Long getLong(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Integer getInteger(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Short getShort(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Byte getByte(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Character getChar(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Boolean getBoolean(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public Object getObject(Key key)
    {
        throw new UnsupportedOperationException("not implemented.");
    }
}
