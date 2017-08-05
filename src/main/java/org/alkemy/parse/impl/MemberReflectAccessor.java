package org.alkemy.parse.impl;

import java.lang.reflect.Field;

import org.alkemy.exception.AlkemyException;

public class MemberReflectAccessor extends StaticReflectAccessor
{
    public MemberReflectAccessor(Field f)
    {
        super(f);
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
