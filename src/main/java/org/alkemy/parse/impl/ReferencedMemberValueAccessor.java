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

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.alkemy.exception.AlkemyException;

public class ReferencedMemberValueAccessor extends ReferencedStaticValueAccessor
{
    public ReferencedMemberValueAccessor(String name, Class<?> type, Class<?> componentType, Function<Object, ?> getter, BiConsumer<Object, Object> setter)
    {
        super(name, type, componentType, getter, setter);
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        return parent != null ? super.get(parent) : null;
    }

    @Override
    public void set(Object value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public void set(String value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public double getDouble(Object parent) throws AlkemyException
    {
        return parent != null ? super.getDouble(parent) : null;
    }

    @Override
    public void set(double value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public float getFloat(Object parent) throws AlkemyException
    {
        return parent != null ? super.getFloat(parent) : null;
    }

    @Override
    public void set(float value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public long getLong(Object parent) throws AlkemyException
    {
        return parent != null ? super.getLong(parent) : null;
    }

    @Override
    public void set(long value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public int getInt(Object parent) throws AlkemyException
    {
        return parent != null ? super.getInt(parent) : null;
    }

    @Override
    public void set(int value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public short getShort(Object parent) throws AlkemyException
    {
        return parent != null ? super.getShort(parent) : null;
    }

    @Override
    public void set(short value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public char getChar(Object parent) throws AlkemyException
    {
        return parent != null ? super.getChar(parent) : null;
    }

    @Override
    public void set(char value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public byte getByte(Object parent) throws AlkemyException
    {
        return parent != null ? super.getByte(parent) : null;
    }

    @Override
    public void set(byte value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }

    @Override
    public boolean getBoolean(Object parent) throws AlkemyException
    {
        return parent != null ? super.getBoolean(parent) : null;
    }

    @Override
    public void set(boolean value, Object parent) throws AlkemyException
    {
        if (parent != null)
            super.set(value, parent);
    }
}
