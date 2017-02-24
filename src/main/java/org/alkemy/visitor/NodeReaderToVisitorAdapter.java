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
package org.alkemy.visitor;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.Nodes.TypifiedNode;

/**
 * Adapter between a reader and a visitor to access the Iterable functionality.
 */
class NodeReaderToVisitorAdapter<R, P> implements AlkemyNodeReader<R, P>, AlkemyNodeVisitor<R, P>
{
    private final AlkemyNodeReader<R, P> reader;
    private final AlkemyElementVisitor<P, ?> aev;

    NodeReaderToVisitorAdapter(AlkemyNodeReader<R, P> reader, AlkemyElementVisitor<P, ?> aev)
    {
        this.reader = reader;
        this.aev = aev;
    }

    @Override
    public R visit(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node)
    {
        return reader.accept(aev, node);
    }

    @Override
    public R visit(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, P parameter)
    {
        return reader.accept(aev, node, parameter);
    }
}
