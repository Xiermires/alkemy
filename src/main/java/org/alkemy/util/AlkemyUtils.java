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
package org.alkemy.util;

import java.lang.reflect.Field;

import org.alkemy.parse.impl.AlkemyElement;

public class AlkemyUtils
{
    public static Object getInstance(Node<? extends AlkemyElement> e, Object parent, boolean newNodeIfNull)
    {
        Object instance = e.data().get(parent);
        if (newNodeIfNull && instance == null)
        {
            instance = e.data().newInstance();
            e.data().set(instance, parent);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    // safe
    public static <T> T getInstance(Node<? extends AlkemyElement> e, Object parent, Class<T> target, boolean newNodeIfNull)
    {
        Object instance = e.data().get(parent);
        if (newNodeIfNull && instance == null)
        {
            instance = e.data().newInstance(target);
            e.data().set(instance, parent);
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> void setEnum(Field f, String name, Object parent) throws IllegalArgumentException,
            IllegalAccessException
    {
        f.set(parent, toEnum((Class<T>) f.getType(), name));
    }

    public static <T extends Enum<T>> T toEnum(Class<T> type, String name)
    {
        return Enum.valueOf(type, name);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Object toEnum(Class<T> type, Object value)
    {
        return value instanceof String ? toEnum(type, (String) value) : (T) value;
    }
}
