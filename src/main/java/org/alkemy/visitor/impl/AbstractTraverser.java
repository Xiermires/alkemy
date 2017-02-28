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
import org.alkemy.util.Assertions;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeReader;

/**
 * Common functionality of some traverses.
 */
public abstract class AbstractTraverser<R, P> implements AlkemyNodeReader<R, P>
{
    /**
     * Branches descending from a null node are also evaluated.
     */
    public static final int INCLUDE_NULL_BRANCHES = 0x1;
    
    /**
     * Instantiate nodes which are null.
     */
    public static final int INSTANTIATE_NODES = 0x2;
    
    /**
     * Nodes are also evaluated by the {@link AlkemyElementVisitor}
     */
    public static final int VISIT_NODES = 0x4;
    
    /**
     * Leafs are ignored.
     */
    public static final int IGNORE_LEAFS = 0x8;
    
    protected boolean visitNodes;
    
    protected AbstractTraverser(boolean visitNodes)
    {
        this.visitNodes = visitNodes;
    }
    
    @Override
    public R create(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root)
    {
        Assertions.nonNull(root);
        final R instance = root.data().safeNewInstance(root.type());
        if (instance != null)
        {
            if (visitNodes)
            {
                root.data().accept(aev, instance);
            }
            root.children().forEach(c -> processBranch(aev, c, instance));
        }
        return instance;
    }

    @Override
    public R create(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root, P parameter)
    {
        Assertions.nonNull(root);

        final R instance = root.data().safeNewInstance(root.type());
        if (instance != null)
        {
            if (visitNodes)
            {
                root.data().accept(aev, instance, parameter);
            }
            root.children().forEach(c -> processBranch(aev, c, instance, parameter));
        }
        return instance;
    }

    protected abstract void processBranch(AlkemyElementVisitor<P, ?> aev, Node<? extends AbstractAlkemyElement<?>> e,
            Object parent, P parameter);

    protected abstract void processBranch(AlkemyElementVisitor<P, ?> aev, Node<? extends AbstractAlkemyElement<?>> e,
            Object parent);
}
