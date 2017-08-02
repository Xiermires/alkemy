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
package org.alkemy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CoreAlkemy
{
    @Test
    public void simpleSetting() {
        final TestClass tc = new TestClass();
        AlkemyNodes.get(TestClass.class).traverse(c -> c.data().set(-1, tc));
        
        assertThat(tc.n1, is(-1));
        assertThat(tc.n2, is(-1));
        assertThat(tc.n3, is(-1));
        assertThat(tc.n4, is(-1));
        assertThat(tc.n5, is(-1));
    }
    
    @Test
    public void simpleGetting() {
        final TestClass tc = new TestClass();
        final Summation sum = new Summation();
        AlkemyNodes.get(TestClass.class).traverse(c -> sum.add(c.data().get(tc, Integer.class)));
        
        assertThat(sum.sum, is(15));
    }
    
    public static class Summation {
        int sum = 0;
        
        void add(int i) {
            sum += i;
        }
    }
}
