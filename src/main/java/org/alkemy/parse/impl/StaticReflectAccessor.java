package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import org.alkemy.exception.AlkemyException;
import org.alkemy.exception.TypeMismatch;
import org.alkemy.util.AlkemyUtils;
import org.alkemy.util.NumberConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticReflectAccessor implements ValueAccessor
{
    private static final Logger log = LoggerFactory.getLogger(StaticReflectAccessor.class);

    private final Class<?> type;
    private final String name;
    private final boolean isEnum;
    private final Field f;
    private MethodHandle getter;
    private MethodHandle setter;

    private final int rank;

    public StaticReflectAccessor(Field f)
    {
        type = f.getType();
        name = f.getDeclaringClass().getTypeName() + "." + f.getName();
        isEnum = type.isEnum();
        rank = NumberConversion.getRank(f.getType());

        try
        {
            getter = MethodHandles.lookup().unreflectGetter(f);
            setter = MethodHandles.lookup().unreflectSetter(f);
        }
        catch (SecurityException | IllegalAccessException e)
        {
            getter = null;
            log.trace("No getter method for field '{}'. Apply direct reflection.", f.toGenericString());
        }

        if (getter == null || setter == null)
        {
            this.f = f;
            f.setAccessible(true);
        }
        else
        {
            this.f = null;
        }
    }

    @Override
    public Class<?> type()
    {
        return type;
    }

    @Override
    public String valueName()
    {
        return name;
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.get(parent) : getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public double getDouble(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getDouble(parent) : (double) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public float getFloat(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getFloat(parent) : (float) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public long getLong(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getLong(parent) : (long) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public int getInt(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getInt(parent) : (int) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public short getShort(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getShort(parent) : (short) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public char getChar(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getChar(parent) : (char) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public byte getByte(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getByte(parent) : (byte) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public boolean getBoolean(Object parent) throws AlkemyException
    {
        try
        {
            return f != null ? f.getBoolean(parent) : (boolean) getter.invoke(parent);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't get value from parent type '{}' for target '{}' of type '{}'", e,
                    parent != null ? parent.getClass().getName() : "null", f.getName(), f.getType().getName());
        }
    }

    @Override
    public void set(Object value, Object parent) throws AlkemyException
    {
        try
        {
            value = rank != -1 ? NumberConversion.convert(value, rank) : value;
            if (f != null)
                f.set(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, value != null ? value.getClass().getName() : "null");
        }
    }

    @Override
    public void set(String value, Object parent) throws AlkemyException
    {
        try
        {
            if (isEnum)
            {
                set(toEnum(type, value), parent);
            }
            else
            {
                if (f != null)
                    f.set(parent, value);
                else setter.invoke(parent, value);
            }
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, value != null ? value.getClass().getName() : "null");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T toEnum(Class<?> type, String name)
    {
        return AlkemyUtils.toEnum((Class<T>) type, name);
    }

    @Override
    public void set(double value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setDouble(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, double.class.getName());
        }
    }

    @Override
    public void set(float value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setFloat(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, float.class.getName());
        }
    }

    @Override
    public void set(long value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setLong(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, long.class.getName());
        }
    }

    @Override
    public void set(int value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setInt(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, int.class.getName());
        }
    }

    @Override
    public void set(short value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setShort(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, short.class.getName());
        }
    }

    @Override
    public void set(char value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setChar(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, char.class.getName());
        }
    }

    @Override
    public void set(byte value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setByte(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, byte.class.getName());
        }
    }

    @Override
    public void set(boolean value, Object parent) throws AlkemyException
    {
        try
        {
            if (f != null)
                f.setBoolean(parent, value);
            else setter.invoke(parent, value);
        }
        catch (Throwable e)
        {
            throw new TypeMismatch("Can't set into type '%s' for target '%s' the value '%s' of type '%s'", e, f.getType()
                    .getName(), f.getName(), value, boolean.class.getName());
        }
    }
}
