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
package org.alkemy.parse;

import org.alkemy.exception.AlkemyException;

public interface ValueAccessor extends AutoCastValueAccessor
{
    /**
     * As {@link #set(Object, Object)} for type String.
     */
    default void set(String value, Object parent) throws AlkemyException
    {
        set((Object) value, parent);
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
