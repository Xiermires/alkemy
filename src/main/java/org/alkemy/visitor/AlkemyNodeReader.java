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
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyNodeHandler.Entry;
import org.alkemy.visitor.impl.NodeReaderToVisitorAdapter;

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
    /**
     * Generates an element of type R.
     */
    default R create(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Generates an element of type R using a parameter P.
     */
    default R create(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, P parameter)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * Generates an element of type R, or modifies and returns the received param1 of type R, using the param2 of type P.
     */
    default R accept(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, R param1, P param2)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * Syntax sugar. Fluent version of the {@link AlkemyNodeReader}.
     * <p>
     * An {@link AlkemyNodeReader} where both parameter and return are from the same type. Usually represents a fluent operation
     * where the parameter object is worked on and returned just after.
     */
    public interface FluentAlkemyNodeReader<R> extends AlkemyNodeReader<R, R>
    {   
    }

    
    /* * STREAM SUPPORT * */

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterable)}
     */
    default Stream<R> stream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    default Stream<R> stream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #peekIterable(TypifiedNode, Iterable)}
     */
    default Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    default Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Supplier)}
     */
    default Stream<R> stream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, node, hasNext).iterator(), -1, 0), false);
    }

    /* * PARALLEL STREAM SUPPORT * */

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Iterable)} 
     */
    default Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, node, items).iterator(), Long.MAX_VALUE, 0), false);
    }

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Iterator)} 
     */
    default Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, node, items).iterator(), Long.MAX_VALUE, 0), false);
    }

    /**
     * Parallel stream of {@link #peekIterable(TypifiedNode, Iterable)}
     */
    default Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, node, items).iterator(), Long.MAX_VALUE, 0), false);
    }
    
    /**
     * Parallel stream of {@link #peekIterable(TypifiedNode, Iterator)}
     */
    default Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, node, items).iterator(), Long.MAX_VALUE, 0), false);
    }

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Supplier)}
     */
    default Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, node, hasNext).iterator(), Long.MAX_VALUE, 0), false);
    }
    
    
    /* * ITERABLE SUPPORT * */
    
    /**
     * See {@link AlkemyNodeHandler#iterable(Node, Iterable)}
     */
    default Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
            Iterable<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).iterable(node, items);
    }

    /**
     * See {@link AlkemyNodeHandler#iterable(Node, Iterator)}
     */
    default Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
            Iterator<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).iterable(node, items);
    }
    
    /**
     * See {@link AlkemyNodeHandler#peekIterable(Node, Iterable)}
     */
    default Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
            Iterable<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).peekIterable(node, items);
    }

    /**
     * See {@link AlkemyNodeHandler#peekIterable(Node, Iterator)}
     */
    default Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
            Iterator<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).peekIterable(node, items);
    }

    /**
     * See {@link AlkemyNodeHandler#iterable(Node, Supplier, Class)}
     */
    default Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
            Supplier<Boolean> hasNext)
    {
        return new NodeReaderToVisitorAdapter<R, P>(this, aev).iterable(node, hasNext);
    }
}
