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

import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alkemy.Alkemy;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeReader;
import org.alkemy.visitor.AlkemyNodeVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor.Entry;

/**
 * Part of the simple Alkemy syntax sugar. See {@link Alkemy}.
 * <p>
 * This class reproduces all Alkemy operations removing the Node parameter from the signature.
 */
public class SingleTypeReader<R, P>
{
    private final TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root;
    private final AlkemyNodeReader<R, P> anv;

    public SingleTypeReader(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root, AlkemyNodeReader<R, P> anv)
    {
        this.root = root;
        this.anv = anv;
    }

    /**
     * Generates an element of type R.
     */
    public R accept(AlkemyElementVisitor<P, ?> aev)
    {
        return anv.accept(aev, root);
    }

    /**
     * Generates an element of type R using a parameter P.
     */
    public R accept(AlkemyElementVisitor<P, ?> aev, P parameter)
    {
        return anv.accept(aev, root, parameter);
    }

    /**
     * Generates an element of type R, or modifies and returns the received param1 of type R, using
     * the param2 of type P.
     */
    public R accept(AlkemyElementVisitor<P, ?> aev, R param1, P param2)
    {
        return anv.accept(aev, root, param1, param2);
    }

    // fluent version.
    public static class FluentSingleTypeReader<R> extends SingleTypeReader<R, R>
    {
        public FluentSingleTypeReader(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root, AlkemyNodeReader<R, R> anv)
        {
            super(root, anv);
        }   
    }
    
    /* * STREAM SUPPORT * */

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterable)}
     */
    public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #peekIterable(TypifiedNode, Iterable)}
     */
    public Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    public Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Supplier)}
     */
    public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, hasNext).iterator(), -1, 0), false);
    }

    /* * PARALLEL STREAM SUPPORT * */

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Iterable)}
     */
    public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
    }

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
    }

    /**
     * Parallel stream of {@link #peekIterable(TypifiedNode, Iterable)}
     */
    public Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
    }

    /**
     * Parallel stream of {@link #peekIterable(TypifiedNode, Iterator)}
     */
    public Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
    }

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Supplier)}
     */
    public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(aev, hasNext).iterator(), Long.MAX_VALUE, 0), false);
    }

    /* * ITERABLE SUPPORT * */

    /**
     * See {@link AlkemyNodeVisitor#iterable(Node, Iterable)}
     */
    public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, items);
    }

    /**
     * See {@link AlkemyNodeVisitor#iterable(Node, Iterator)}
     */
    public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, items);
    }

    /**
     * See {@link AlkemyNodeVisitor#peekIterable(Node, Iterable)}
     */
    public Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(anv, aev).peekIterable(root, items);
    }

    /**
     * See {@link AlkemyNodeVisitor#peekIterable(Node, Iterator)}
     */
    public Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
    {
        return new NodeReaderToVisitorAdapter<R, P>(anv, aev).peekIterable(root, items);
    }

    /**
     * See {@link AlkemyNodeVisitor#iterable(Node, Supplier, Class)}
     */
    public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
    {
        return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, hasNext);
    }
}
