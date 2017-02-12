package org.alkemy;

import org.alkemy.common.BindParentReference;

public class AssignConstant<T> extends BindParentReference
{
    private final T t;
    
    AssignConstant(T t)
    {
        this.t = t;
    }
    
    @Override
    protected void visit(AlkemyElement e, Object parent)
    {
        e.set(t, parent);
    }
}
