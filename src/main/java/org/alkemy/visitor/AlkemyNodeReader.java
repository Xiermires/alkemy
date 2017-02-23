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

import java.util.Iterator;
import java.util.function.Supplier;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyNodeVisitor.Entry;
import org.alkemy.visitor.AlkemyNodeVisitor.FluentAlkemyNodeVisitor;

/**
 * A class implementing this interface is expected to process trees of alkemy elements and delegate its element processing to
 * {@link AlkemyElementVisitor}s.
 * <p>
 * Classes implementing this interface are expected to provide a meaningful traverse strategy useful to multiple
 * {@link AlkemyElementVisitor}.
 * 
 * @param <R>
 *            return type
 * @param <P>
 *            parameter type
 */
public interface AlkemyNodeReader<R, P>
{
    default R accept(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node, Class<R> retType)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    default R accept(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node, P parameter, Class<R> retType)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Fluent version of the {@link AlkemyNodeReader}.
     */
    public interface FluentAlkemyNodeReader<R> extends AlkemyNodeReader<R, R>
    {
        default R accept(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node, R parameter)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        /**
         * See {@link FluentAlkemyNodeVisitor#iterable(Node, Iterable)}
         */
        default Iterable<R> iterable(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node, Iterable<R> items)
        {
            return new FluentNodeReaderToVisitorAdapter<R>(this, aev).iterable(node, items);
        }

        /**
         * See {@link FluentAlkemyNodeVisitor#iterable(Node, Iterator)}
         */
        default Iterable<R> iterable(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node, Iterator<R> items)
        {
            return new FluentNodeReaderToVisitorAdapter<R>(this, aev).iterable(node, items);
        }
    }

    /**
     * See {@link AlkemyNodeVisitor#iterable(Node, Iterable, Class)}
     */
    default Iterable<Entry<R, P>> iterable(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node,
            Iterable<P> items, Class<R> retType)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).iterable(node, items, retType);
    }

    /**
     * See {@link AlkemyNodeVisitor#iterable(Node, Iterator, Class)}
     */
    default Iterable<Entry<R, P>> iterable(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node,
            Iterator<P> items, Class<R> retType)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).iterable(node, items, retType);
    }

    /**
     * See {@link AlkemyNodeVisitor#iterable(Node, Supplier, Class)}
     */
    default Iterable<R> iterable(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> node,
            Supplier<Boolean> hasNext, Class<R> retType)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).iterable(node, hasNext, retType);
    }
}
