package org.alkemy.common;

import org.alkemy.core.AlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyElementVisitor;

public abstract class BindParentReference implements AlkemyElementVisitor
{
    private Object bound;
    
    @Override
    public void bind(Object t)
    {
        this.bound = t;
    }
    
    @Override
    public Object bound()
    {
        return bound;
    }

    @Override
    public void visit(Node<? extends AlkemyElement> e)
    {
        visit(e, bound);        
    }
    
    // TODO: Translate to traverse if possible.
    private void visit(Node<? extends AlkemyElement> e, Object ref)
    {
        e.children().forEach(n ->
        {
            n.data().bind(ref);
            visit(n.data());
            if (n.hasChildren())
            {
                n.children().forEach(nn -> visit(nn, n.data().get()));
            }
        });
    }
    
    protected abstract void visit(AlkemyElement e);
}
