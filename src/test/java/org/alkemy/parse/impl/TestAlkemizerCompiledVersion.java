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
package org.alkemy.parse.impl;


public class TestAlkemizerCompiledVersion
{
    TestAlkemizerCompiledVersion()
    {   
    }
    
    public TestAlkemizerCompiledVersion(int foo, String bar)
    {
        this.foo = foo;
        this.bar = bar;
    }
    
    @Foo
    private int foo = -1;
    
    @Foo
    private String bar;
    
    public static TestAlkemizerCompiledVersion create$$instance(int foo, String bar)
    {
        final TestAlkemizerCompiledVersion tacv = new TestAlkemizerCompiledVersion();
        tacv.foo = foo;
        tacv.bar = bar;
        return tacv;
    }
    
    public static boolean is$$instrumented()
    {
        return true;
    }
    
    public int get$$foo()
    {
        return foo;
    }
    
    public void set$$foo(final int foo)
    {
        this.foo = foo;
    }
    
    public String get$$bar()
    {
        return bar;
    }
    
    public void set$$bar(final String bar)
    {
        this.bar = bar;
    }
}
