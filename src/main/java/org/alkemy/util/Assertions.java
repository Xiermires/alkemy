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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.alkemy.exception.AlkemyException;
import org.alkemy.exception.InvalidArgument;

// program with assertions having control over the exceptions types.
public class Assertions
{
    public static void noneNull(Object... o)
    {
        for (int i = 0; i < o.length; i++)
            Objects.requireNonNull(o[i]);
    }

    public static void nonNull(Object o)
    {
        Objects.requireNonNull(o);
    }

    public static void notEmpty(boolean[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(char[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(byte[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(short[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(int[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(long[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(float[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(double[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void notEmpty(Object[] o)
    {
        nonNull(o);
        if (o.length == 0) throw new ArrayIndexOutOfBoundsException();
    }

    public static void ofSize(boolean[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(char[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(byte[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(short[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(int[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(long[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(float[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(double[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(Object[] o, int size)
    {
        nonNull(o);
        if (o.length != size) throw new InvalidArgument("Invalid array size { expected '%d', received '%d' }", size, o.length);
    }

    public static void ofSize(Collection<?> l, int size)
    {
        nonNull(l);
        if (l.size() != size)
            throw new InvalidArgument("Invalid collection size { expected '%d', received '%d' }", size, l.size());
    }

    public static void ofListedType(Object o, Class<?>... types)
    {
        nonNull(o);
        for (Class<?> type : types)
            if (o.getClass().equals(type)) return;

        throw new InvalidArgument("Invalid class type { expected '%s', received '%s' }", Arrays.asList(
                types).toString(), o.getClass().getName());
    }

    public static <T> void isTrue(boolean expr, String format, Object... args)
    {
        if (!expr) throw new AlkemyException(format, args);
    }

    public static void lessEqualThan(long lhs, long rhs)
    {
        if (lhs <= rhs) return;
        throw new InvalidArgument("Invalid argument { expected less or equal than '%d', received '%d' }", rhs, lhs);
    }

    public static void lessThan(long lhs, long rhs)
    {
        if (lhs < rhs) return;
        throw new InvalidArgument("Invalid argument { expected less than '%d', received '%d' }", rhs, lhs);
    }
    
    public static void greaterEqualThan(long lhs, long rhs)
    {
        if (lhs >= rhs) return;
        throw new InvalidArgument("Invalid argument { expected greater or equal than '%d', received '%d' }", rhs, lhs);
    }

    public static void greaterThan(long lhs, long rhs)
    {
        if (lhs > rhs) return;
        throw new InvalidArgument("Invalid argument { expected greater than '%d', received '%d' }", rhs, lhs);
    }
}
