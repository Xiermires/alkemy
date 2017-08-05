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

public class NumberConversion
{
    private static final int BYTE = 0;
    private static final int SHORT = 1;
    private static final int INT = 2;
    private static final int LONG = 3;
    private static final int FLOAT = 4;
    private static final int DOUBLE = 5;

    /**
     * Returns the numeric wrapper type ranked from [0 to 5] corresponding to the numeric wrapper sorted by min/max representable
     * numbers.
     * <p>
     * Returns {@link Double#NaN} if class is not a numeric wrapper.
     */
    public static int getRank(Class<?> wrapper)
    {
        if (Integer.class == wrapper || int.class == wrapper)
        {
            return INT;
        }
        else if (Double.class == wrapper || double.class == wrapper)
        {
            return DOUBLE;
        }
        else if (Float.class == wrapper || float.class == wrapper)
        {
            return FLOAT;
        }
        else if (Long.class == wrapper || long.class == wrapper)
        {
            return LONG;
        }
        else if (Short.class == wrapper || short.class == wrapper)
        {
            return SHORT;
        }
        else if (Byte.class == wrapper || byte.class == wrapper)
        {
            return BYTE;
        }
        return -1; // no convertible number.
    }

    public static Number convert(Object n, int rank)
    {
        if (n instanceof Number)
        {
            if (INT == rank)
            {
                return ((Number) n).intValue();
            }
            else if (DOUBLE == rank)
            {
                return ((Number) n).doubleValue();
            }
            else if (FLOAT == rank)
            {
                return ((Number) n).floatValue();
            }
            else if (LONG == rank)
            {
                return ((Number) n).longValue();
            }
            else if (SHORT == rank)
            {
                return ((Number) n).shortValue();
            }
            else if (BYTE == rank)
            {
                return ((Number) n).byteValue();
            }
        }
        return null;
    }
    
    public static Number convert(String n, int rank)
    {
            if (INT == rank)
            {
                return Integer.valueOf(n);
            }
            else if (DOUBLE == rank)
            {
                return Double.valueOf(n);
            }
            else if (FLOAT == rank)
            {
                return Float.valueOf(n);
            }
            else if (LONG == rank)
            {
                return Long.valueOf(n);
            }
            else if (SHORT == rank)
            {
                return Short.valueOf(n);
            }
            else if (BYTE == rank)
            {
                return Byte.valueOf(n);
            }
        
        return null;
    }
}
