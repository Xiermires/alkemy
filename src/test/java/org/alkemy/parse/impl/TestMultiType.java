package org.alkemy.parse.impl;

import org.alkemy.Foo;

public class TestMultiType
{
    @Foo
    private int i;
    
    @Foo
    private Integer I;
    
    public int getFoo() {
        return i;
    }
    
    public void setFoo(int i) {
        this.i = i;
    }
    
    public void setFoo(Integer I) {
        this.I = I;
    }
}
