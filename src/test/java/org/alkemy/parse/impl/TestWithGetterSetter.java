package org.alkemy.parse.impl;

import org.alkemy.Bar;

public class TestWithGetterSetter
{
    enum Mode { A, B };
    
    @Bar
    int foo;
    
    @Bar
    String bar;
    
    @Bar
    Mode qux = Mode.A;

    public int getFoo()
    {
        return foo;
    }

    public void setFoo(int foo)
    {
        this.foo = foo;
    }

    public String getBar()
    {
        return bar;
    }

    public void setBar(String bar)
    {
        this.bar = bar;
    }

    public Mode getQux()
    {
        return qux;
    }

    public void setQux(Mode qux)
    {
        this.qux = qux;
    }
}
