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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.alkemy.exception.InvalidArgument;

public class Conditions
{
    public static void requireNonNull(Object... o)
    {
        for (int i=0; i<o.length; i++)
            Objects.requireNonNull(o[i]);
    }
    
    public static void requireNotEmpty(boolean[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(char[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(byte[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(short[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(int[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(long[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(float[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(double[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void requireNotEmpty(Object[] o)
    {
        requireNonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }
    
    public static void requireArraySize(boolean[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(char[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(byte[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(short[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(int[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(long[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(float[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(double[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void requireArraySize(Object[] o, int size)
    {
        requireNonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }
    
    public static void requireOfType(Object o, Class<?> type)
    {
        requireNonNull(o);
        if (o.getClass().equals(type)) throw new InvalidArgument("Invalid class type { expected '%d', received '%d' }", o.getClass().getName(), type.getName());
    }
    
    public static <T> boolean isTrue(Predicate<T> p, T t)
    {
        requireNonNull(p);
        return true == p.test(t);
    }

    public static void requireCollectionSize(List<Method> l, int size)
    {
        requireNonNull(l);
        if (l.size() != size) throw new InvalidArgument("Invalid collection size { expected '%d', received '%d' }", size, l.size());
    }
}
