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
package org.alkemy.common;

import org.alkemy.AlkemyElement;
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
            visit(n.data(), ref);
            if (n.hasChildren())
            {
                n.children().forEach(nn -> visit(nn, n.data().get(ref)));
            }
        });
    }
    
    protected abstract void visit(AlkemyElement e, Object parent);
}