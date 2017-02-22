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

import org.junit.Test;

public class ConditionsTest
{
    @Test(expected = NullPointerException.class)
    public void requiresNonNull()
    {
        Object o = null;
        Assertions.nonNull(o);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyBoolean()
    {
        Assertions.notEmpty(new boolean[0]);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyChar()
    {
        Assertions.notEmpty(new char[0]);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyByte()
    {
        Assertions.notEmpty(new byte[0]);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyShort()
    {
        Assertions.notEmpty(new short[0]);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyInt()
    {
        Assertions.notEmpty(new int[0]);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyLong()
    {
        Assertions.notEmpty(new long[0]);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyFloat()
    {
        Assertions.notEmpty(new float[0]);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyDouble()
    {
        Assertions.notEmpty(new double[0]);
    }   
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void requiresNotEmptyObjArray()
    {
        Assertions.notEmpty(new Object[0]);
    }
}
