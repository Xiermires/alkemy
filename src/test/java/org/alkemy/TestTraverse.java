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
