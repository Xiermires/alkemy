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
package org.alkemy.parse.impl;

import org.alkemy.Bar;
import org.alkemy.Foo;
import org.alkemy.parse.impl.TestAlkemizer.Lorem;
import org.alkemy.util.AlkemyUtils;


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
    
    @Bar
    Lorem ipsum;

    @Bar
    float dolor;
    
    public static TestAlkemizerCompiledVersion create$$args(int foo, String bar, Object ipsum, float dolor)
    {
        final TestAlkemizerCompiledVersion tacv = new TestAlkemizerCompiledVersion();
        tacv.foo = foo;
        tacv.bar = bar;
        tacv.ipsum = ipsum instanceof String ? AlkemyUtils.toEnum(Lorem.class, (String) ipsum) : (Lorem) ipsum;
        tacv.dolor = dolor;
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
