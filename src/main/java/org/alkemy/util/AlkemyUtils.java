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
