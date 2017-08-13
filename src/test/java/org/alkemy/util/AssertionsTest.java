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

import org.junit.Test;

public class AssertionsTest
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
