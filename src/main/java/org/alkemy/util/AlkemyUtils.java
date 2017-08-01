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
import java.util.function.BiConsumer;

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
    public static <T> T getInstance(Node<? extends AlkemyElement> e, Object parent, Class<T> target,
            boolean newNodeIfNull)
    {
        Object instance = e.data().get(parent);
        if (newNodeIfNull && instance == null)
        {
            instance = e.data().newInstance(target);
            e.data().set(instance, parent);
        }
        return (T) instance;
    }

    public static void setEnum(Class<?> type, BiConsumer<Object, Object> setter, Object value, Object parent)
    {
        setter.accept(parent, toEnum(type, value));
    }

    public static void setEnum(Field f, Object value, Object parent) throws IllegalArgumentException, IllegalAccessException
    {
        f.set(parent, toEnum(f.getType(), value));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    // caller responsibility
    public static Object toEnum(Class<?> type, Object value)
    {
        if (value instanceof String)
        {
            return Enum.valueOf((Class<? extends Enum>) type, (String) value);
        }
        else return value;
    }
}
