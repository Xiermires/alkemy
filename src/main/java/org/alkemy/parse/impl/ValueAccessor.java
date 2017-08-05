package org.alkemy.parse.impl;

import org.alkemy.exception.AlkemyException;

public interface ValueAccessor extends AutoCastValueAccessor
{
    /**
     * As {@link #set(Object, Object)} for type String.
     */
    default void set(String value, Object parent) throws AlkemyException
    {
        set(value, parent);
    }

    /**
     * As {@link #set(Object, Object)} for type double.
     */
    default void set(double value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a double.
     */
    default double getDouble(Object parent) throws AlkemyException
    {
        return get(parent, Double.class);
    }

    /**
     * As {@link #set(Object, Object)} for type float.
     */
    default void set(float value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a float.
     */
    default float getFloat(Object parent) throws AlkemyException
    {
        return get(parent, Float.class);
    }

    /**
     * As {@link #set(Object, Object)} for type long.
     */
    default void set(long value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a long.
     */
    default long getLong(Object parent) throws AlkemyException
    {
        return get(parent, Long.class);
    }

    /**
     * As {@link #set(Object, Object)} for type int.
     */
    default void set(int value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a int.
     */
    default int getInt(Object parent) throws AlkemyException
    {
        return get(parent, Integer.class);
    }

    /**
     * As {@link #set(Object, Object)} for type short.
     */
    default void set(short value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a short.
     */
    default short getShort(Object parent) throws AlkemyException
    {
        return get(parent, Short.class);
    }

    /**
     * As {@link #set(Object, Object)} for type char.
     */
    default void set(char value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a char.
     */
    default char getChar(Object parent) throws AlkemyException
    {
        return get(parent, Character.class);
    }

    /**
     * As {@link #set(Object, Object)} for type byte.
     */
    default void set(byte value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a byte.
     */
    default byte getByte(Object parent) throws AlkemyException
    {
        return get(parent, Byte.class);
    }

    /**
     * As {@link #set(Object, Object)} for type boolean.
     */
    default void set(boolean value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
    }

    /**
     * Returns the value as a boolean.
     */
    default boolean getBoolean(Object parent) throws AlkemyException
    {
        return get(parent, Boolean.class);
    }
}
