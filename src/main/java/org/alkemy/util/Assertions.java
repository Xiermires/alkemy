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

// program with assertions having control over the exceptions types.
public class Assertions
{
    public static void exist(Object... o)
    {
        for (int i = 0; i < o.length; i++)
            Objects.requireNonNull(o[i]);
    }

    public static void exists(Object o)
    {
        Objects.requireNonNull(o);
    }

    public static void notEmpty(boolean[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(char[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(byte[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(short[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(int[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(long[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(float[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(double[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(Object[] o)
    {
        exists(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void ofSize(boolean[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(char[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(byte[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(short[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(int[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(long[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(float[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(double[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(Object[] o, int size)
    {
        exists(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(Object o, Class<?> type)
    {
        exists(o);
        if (o.getClass().equals(type)) throw new InvalidArgument("Invalid class type { expected '%d', received '%d' }", o
                .getClass().getName(), type.getName());
    }

    public static <T> boolean isTrue(Predicate<T> p, T t)
    {
        exists(p);
        return true == p.test(t);
    }

    public static void ofSize(List<Method> l, int size)
    {
        exists(l);
        if (l.size() != size) throw new InvalidArgument("Invalid collection size { expected '%d', received '%d' }", size,
                l.size());
    }
}
