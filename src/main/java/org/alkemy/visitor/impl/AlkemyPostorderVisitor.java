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

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.AlkemyUtils;
import org.alkemy.util.Assertions;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor;

/**
 * Traverses the directed rooted tree in post-order, branch children first, branch node later.
 */
public class AlkemyPostorderVisitor implements AlkemyNodeVisitor
{
    private AlkemyElementVisitor<?> aev;
    private boolean includeNullNodes;
    private boolean instantiateNodes;
    private boolean visitNodes;

    AlkemyPostorderVisitor(AlkemyElementVisitor<?> aev, boolean includeNullNodes, boolean instantiateNodes, boolean visitNodes)
    {
        this.aev = aev;
        this.includeNullNodes = includeNullNodes;
        this.instantiateNodes = instantiateNodes;
        this.visitNodes = visitNodes;
    }

    public static <E extends AbstractAlkemyElement<E>> AlkemyNodeVisitor create(AlkemyElementVisitor<E> aev,
            boolean includeNullNodes, boolean instantiateNodes, boolean visitNodes)
    {
        return new AlkemyPostorderVisitor(aev, includeNullNodes, instantiateNodes, visitNodes);
    }

    @Override
    public Object visit(Node<? extends AbstractAlkemyElement<?>> root)
    {
        Assertions.exists(root);

        final Object instance = root.data().newInstance();
        root.children().forEach(c -> processBranch(c, instance));
        return instance;
    }

    @Override
    public Object visit(Node<? extends AbstractAlkemyElement<?>> root, Object parent, Object... args)
    {
        Assertions.exists(root);

        root.children().forEach(c -> processBranch(c, parent, args));
        return parent;
    }

    private void processBranch(Node<? extends AbstractAlkemyElement<?>> e, Object parent, Object... args)
    {
        if (e.hasChildren())
        {
            final Object node = AlkemyUtils.getNodeInstance(e, parent, instantiateNodes);
            if (includeNullNodes || node != null)
            {
                e.children().forEach(c ->
                {
                    processBranch(c, c.data().get(node));
                });
                if (visitNodes) e.data().acceptArgs(aev, e.data().get(parent), args);
            }
        }
        else
        {
            e.data().acceptArgs(aev, parent, args);
        }
    }
}
