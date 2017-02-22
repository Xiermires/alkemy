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

import org.alkemy.AssignConstant.Bar;
import org.alkemy.PropertyConcatenation.Foo;
import org.alkemy.annotations.Order;

@Order({ "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9" })
public class TestClassNoInstr
{
    @Foo
    String s0 = "0";

    @Foo
    String s1 = "1";

    @Foo
    String s2 = "2";

    @Foo
    String s3 = "3";

    @Foo
    String s4 = "4";

    @Bar
    String s5 = "5";

    @Bar
    String s6 = "6";

    @Bar
    String s7 = "7";

    @Bar
    String s8 = "8";

    @Bar
    String s9 = "9";
}
