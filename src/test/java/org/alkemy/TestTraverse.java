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

public class TestTraverse
{
    @Bar
    int a = 55;

    @Bar
    int b = 55;

    @Bar
    int c = 55;

    @Bar
    int d = 55;

    NestedA na = new NestedA();

    NestedB nb = new NestedB();

    NestedC nc = new NestedC();

    public static class NestedA
    {
        @Bar
        int a1 = 55;

        NestedD nad = new NestedD();

        @Bar
        int a2 = 55;
    }

    public static class NestedB
    {
        @Bar
        int b1 = 55;

        @Bar
        int b2 = 55;

        NestedE nbe = new NestedE();
    }

    public static class NestedC
    {
        NestedF ncf = new NestedF();

        @Bar
        int c1 = 55;

        @Bar
        int c2 = 55;
    }

    public static class NestedD
    {
        @Bar
        int d1 = 55;

        @Bar
        int d2 = 55;
    }

    public static class NestedE
    {
        @Bar
        int e1 = 55;

        @Bar
        int e2 = 55;
    }

    public static class NestedF
    {
        @Bar
        int f1 = 55;

        @Bar
        int f2 = 55;
    }
}
