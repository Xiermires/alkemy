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
package org.alkemy.visitor.impl;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.util.Conditions;
import org.alkemy.util.Node;
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor;

// pre-order.
public class AlkemyElementReader implements AlkemyNodeVisitor
{
    private AlkemyElementVisitor<?, Object> aev;
    private boolean includeNullNodes;

    public AlkemyElementReader(AlkemyElementVisitor<?, Object> aev, boolean includeNullNodes)
    {
        this.aev = aev;
        this.includeNullNodes = includeNullNodes;
    }

    @Override
    public void visit(Node<? extends AbstractAlkemyElement<?>> root, Reference<Object> rootObj)
    {
        Conditions.requireNonNull(root);
        root.children().forEach(c -> processNode(c, rootObj.get()));
    }

    private void processNode(Node<? extends AbstractAlkemyElement<?>> e, Object parent)
    {
        if (e.hasChildren())
        {
            final Object node = e.data().get(parent);
            if (includeNullNodes || node != null)
            {
                e.children().forEach(c -> processNode(c, node));
            }
        }
        else
        {
            processLeaf(parent, e);
        }
    }

    private void processLeaf(Object parent, Node<? extends AbstractAlkemyElement<?>> e)
    {
        if (aev.accepts(e.data().visitorType()))
        {
            e.data().accept(Reference.inOut(parent), aev);
        }
    }
}
