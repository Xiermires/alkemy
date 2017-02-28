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

/**
 * A class implementing this interface is expected to process trees of alkemy elements w/o
 * delegating element processing to any other sources.
 * <p>
 * Differently to the {@link AlkemyNodeReader}, this class shouldn't delegate the item processing to
 * an {@link AlkemyElementVisitor} , but is both responsible of traversing the tree and its leafs.
 * 
 * @param <R>
 *            return type
 * @param <P>
 *            parameter type
 */
public interface AlkemyNodeHandler<R, P>
{
    /**
     * Generates an element of type R.
     */
    default R create(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Generates an element of type R using a parameter P.
     */
    default R create(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, P parameter)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Generates an element of type R, or modifies and returns the received param1 of type R.
     */
    default R handle(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, R parameter)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * Generates an element of type R, or modifies and returns the received param1 of type R, using
     * the param2 of type P.
     */
    default R handle(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, R param1, P param2)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /* * STREAM SUPPORT * */

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterable)}
     */
    default Stream<R> stream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<R> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    default Stream<R> stream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<R> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #peekIterable(TypifiedNode, Iterable)}
     */
    default Stream<Entry<R, P>> peekStream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    default Stream<Entry<R, P>> peekStream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(node, items).iterator(), -1, 0), false);
    }

    /**
     * Stream of {@link #iterable(TypifiedNode, Supplier)}
     */
    default Stream<R> stream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(node, hasNext).iterator(), -1, 0), false);
    }

    /* * PARALLEL STREAM SUPPORT * */

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Iterable)}
     */
    default Stream<R> parallelStream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<R> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(node, items).iterator(), Long.MAX_VALUE, 0), true);
    }

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Iterator)}
     */
    default Stream<R> parallelStream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<R> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(node, items).iterator(), Long.MAX_VALUE, 0), true);
    }

    /**
     * Parallel stream of {@link #peekIterable(TypifiedNode, Iterable)}
     */
    default Stream<Entry<R, P>> parallelPeekStream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(node, items).iterator(), Long.MAX_VALUE, 0), true);
    }

    /**
     * Parallel stream of {@link #peekIterable(TypifiedNode, Iterator)}
     */
    default Stream<Entry<R, P>> parallelPeekStream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
    {
        return StreamSupport.stream(Spliterators.spliterator(peekIterable(node, items).iterator(), Long.MAX_VALUE, 0), true);
    }

    /**
     * Parallel stream of {@link #iterable(TypifiedNode, Supplier)}
     */
    default Stream<R> parallelStream(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext)
    {
        return StreamSupport.stream(Spliterators.spliterator(iterable(node, hasNext).iterator(), Long.MAX_VALUE, 0), true);
    }

    /* * ITERABLE SUPPORT * */

    /**
     * Returns an iterable of type R. Items returned might be new or modified from items.
     * <p>
     * There is a 1:1 relation between input and output iterables.
     * <p>
     * Items are lazily fetched.
     */
    default Iterable<R> iterable(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<R> items)
    {
        return new FluentIterable<R, P>(this, node, items.iterator());
    }

    /**
     * Syntax sugar. See {@link #iterable(Node, Iterable)}
     */
    default Iterable<R> iterable(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<R> items)
    {
        return new FluentIterable<R, P>(this, node, items);
    }

    /**
     * Returns an iterable of {@link Entry}. An entry contains the result of applying the
     * {@link AlkemyNodeHandler} to the node
     * with the current P item and a peek into the future P to be used on the next iteration.
     * <p>
     * The first iteration contains always no result (null) and the first P item in items.
     * <p>
     * There is a 1:1 relation between P and T items.
     * <p>
     * Items are lazily fetched.
     * <p>
     * Example:
     * <p>
     * If we have an Iterable &lt;P&gt; containing {1, 2, 3, 4} and applying
     * {@link AlkemyNodeHandler} to each P would result in the following iterator {2, 3, 4, 5} (P +
     * 1). <br>
     * The {@link #peekIterable(TypifiedNode, Iterable)} function will return the following Entries:
     * <ol>
     * <li>Entry 1 : {null, 1}
     * <li>Entry 2 : {1, 2}
     * <li>Entry 3 : {2, 3}
     * <li>Entry 4 : {3, 4}
     * <li>Entry 5 : {4, 5}
     * <li>Entry 6 : {5, null}
     * </ol>
     * The iterator finishes ({@link Iterator#hasNext()} equals false) when the
     * {@link AlkemyNodeHandler} has been applied to the all P, hence after Entry 6.
     */
    default Iterable<Entry<R, P>> peekIterable(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterable<P> items)
    {
        return new PeekIterable<R, P>(this, node, items.iterator());
    }

    /**
     * Syntax sugar. See {@link #iterable(Node, Iterable)}
     */
    default Iterable<Entry<R, P>> peekIterable(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
    {
        return new PeekIterable<R, P>(this, node, items);
    }

    /**
     * Returns an iterable of type T.
     * <p>
     * The items are lazily generated on each {@link Iterator#next()} call until the hasNext
     * function returns false.
     */
    default Iterable<R> iterable(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext)
    {
        return new CreateIterable<R, P>(this, node, hasNext);
    }

    static abstract class AbstractIter<R, P>
    {
        protected final AlkemyNodeHandler<R, P> visitor;
        protected final TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node;

        protected AbstractIter(AlkemyNodeHandler<R, P> visitor, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node)
        {
            this.visitor = visitor;
            this.node = node;
        }
    }

    static class PeekIterable<R, P> extends AbstractIter<R, P> implements Iterable<Entry<R, P>>
    {
        private final Iterator<P> items;

        PeekIterable(AlkemyNodeHandler<R, P> visitor, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
        {
            super(visitor, node);
            this.items = items;
        }

        @Override
        public Iterator<Entry<R, P>> iterator()
        {
            return new PeekIterator<R, P>(visitor, node, items);
        }
    }

    static class CreateIterable<R, P> extends AbstractIter<R, P> implements Iterable<R>
    {
        private final Supplier<Boolean> hasNext;

        CreateIterable(AlkemyNodeHandler<R, P> visitor, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
                Supplier<Boolean> hasNext)
        {
            super(visitor, node);
            this.hasNext = hasNext;
        }

        @Override
        public Iterator<R> iterator()
        {
            return new CreateIterator<R, P>(visitor, node, hasNext);
        }
    }

    static class FluentIterable<R, P> extends AbstractIter<R, P> implements Iterable<R>
    {
        private final Iterator<R> items;

        FluentIterable(AlkemyNodeHandler<R, P> visitor, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
                Iterator<R> items)
        {
            super(visitor, node);
            this.items = items;
        }

        @Override
        public Iterator<R> iterator()
        {
            return new FluentIterator<R, P>(visitor, node, items);
        }
    }

    static class PeekIterator<R, P> extends AbstractIter<R, P> implements Iterator<Entry<R, P>>
    {
        private boolean first = true;
        private P next;
        private final Iterator<P> items;

        PeekIterator(AlkemyNodeHandler<R, P> visitor, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Iterator<P> items)
        {
            super(visitor, node);
            this.items = items;
            this.next = items.hasNext() ? items.next() : null;
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        @Override
        public Entry<R, P> next()
        {
            if (first)
            {
                first = false;
                return new Entry<R, P>(null, next);
            }
            else
            {
                final R result = visitor.create(node, next);
                next = items.hasNext() ? items.next() : null;
                return new Entry<R, P>(result, next);
            }
        }
    }

    static class CreateIterator<R, P> extends AbstractIter<R, P> implements Iterator<R>
    {
        private final Supplier<Boolean> hasNext;

        CreateIterator(AlkemyNodeHandler<R, P> visitor, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
                Supplier<Boolean> hasNext)
        {
            super(visitor, node);
            this.hasNext = hasNext;
        }

        @Override
        public boolean hasNext()
        {
            return hasNext.get();
        }

        @Override
        public R next()
        {
            return visitor.create(node);
        }
    }

    static class FluentIterator<R, P> extends AbstractIter<R, P> implements Iterator<R>
    {
        private final Iterator<R> items;

        FluentIterator(AlkemyNodeHandler<R, P> visitor, TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node,
                Iterator<R> items)
        {
            super(visitor, node);
            this.items = items;
        }

        @Override
        public boolean hasNext()
        {
            return items.hasNext();
        }

        @Override
        public R next()
        {
            return visitor.handle(node, items.next());
        }
    }

    public static class Entry<R, P> implements Cloneable
    {
        private R r;
        private P p;

        Entry(R r, P p)
        {
            this.r = r;
            this.p = p;
        }

        public R result()
        {
            return r;
        }

        public P peekNext()
        {
            return p;
        }

        @Override
        public Entry<R, P> clone()
        {
            return new Entry<R, P>(r, p);
        }
    }
}
