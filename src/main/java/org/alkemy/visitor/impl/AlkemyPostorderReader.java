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
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeReader.FluentAlkemyNodeReader;

/**
 * Traverses the directed rooted tree in post-order, branch children first, branch node later.
 */
public class AlkemyPostorderReader<R, P> extends AbstractTraverser<R, P>
{
    private boolean includeNullNodes;
    private boolean instantiateNodes;
    private boolean includeLeafs;

    /**
     * Configure using {@code INCLUDE_NULL_BRANCHES} | {@code INSTANTIATE_NODES} |
     * {@code VISIT_NODES}
     */
    public AlkemyPostorderReader(int conf)
    {
        super((conf & VISIT_NODES) != 0);
        this.includeNullNodes = (conf & INCLUDE_NULL_BRANCHES) != 0;
        this.instantiateNodes = (conf & INSTANTIATE_NODES) != 0;
        this.includeLeafs = !((conf & IGNORE_LEAFS) != 0);
    }

    @Override
    protected void processBranch(AlkemyElementVisitor<P, ?> aev, Node<? extends AbstractAlkemyElement<?>> e, Object parent,
            P parameter)
    {
        if (e.hasChildren())
        {
            final Object node = AlkemyUtils.getNodeInstance(e, parent, instantiateNodes);
            if (includeNullNodes || node != null)
            {
                e.children().forEach(c ->
                {
                    processBranch(aev, c, c.data().get(node), parameter);
                });
                if (visitNodes) e.data().accept(aev, e.data().get(parent), parameter);
            }
        }
        else
        {
            if (includeLeafs)
            {
                e.data().accept(aev, parent, parameter);
            }
        }
    }

    @Override
    protected void processBranch(AlkemyElementVisitor<P, ?> aev, Node<? extends AbstractAlkemyElement<?>> e, Object parent)
    {
        if (e.hasChildren())
        {
            final Object node = AlkemyUtils.getNodeInstance(e, parent, instantiateNodes);
            if (includeNullNodes || node != null)
            {
                e.children().forEach(c ->
                {
                    processBranch(aev, c, c.data().get(node));
                });
                if (visitNodes) e.data().accept(aev, e.data().get(parent));
            }
        }
        else
        {
            e.data().accept(aev, parent);
        }
    }

    /**
     * Fluent version. See {@link FluentAlkemyNodeReader}.
     */
    public static class FluentAlkemyPostorderReader<R> extends AlkemyPostorderReader<R, R> implements FluentAlkemyNodeReader<R>
    {
        public FluentAlkemyPostorderReader(int conf)
        {
            super(conf);
        }

        @Override
        public R create(AlkemyElementVisitor<R, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root, R parent)
        {
            Assertions.nonNull(root);

            if (visitNodes)
            {
                root.data().accept(aev, parent);
            }
            root.children().forEach(c -> processBranch(aev, c, parent));
            return parent;
        }
    }
}
