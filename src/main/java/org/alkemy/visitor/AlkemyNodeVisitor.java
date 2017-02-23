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

/**
 * A class implementing this interface is expected to process trees of alkemy elements w/o delegating element processing to any
 * other sources.
 * <p>
 * Differently to the {@link AlkemyNodeReader}, this class shouldn't delegate the item processing to an
 * {@link AlkemyElementVisitor} , but is both responsible of traversing the tree and visiting its leafs.
 * 
 * @param <R>
 *            return type
 * @param <P>
 *            parameter type
 */
public interface AlkemyNodeVisitor<R, P>
{
    default R visit(Node<? extends AbstractAlkemyElement<?>> node, Class<R> retType)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    default R visit(Node<? extends AbstractAlkemyElement<?>> node, P parameter, Class<R> retType)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Fluent version of the {@link AlkemyNodeVisitor}.
     */
    public interface FluentAlkemyNodeVisitor<R> extends AlkemyNodeVisitor<R, R>
    {
        default R visit(Node<? extends AbstractAlkemyElement<?>> node, R parameter)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        /**
         * Returns an iterable of type T. Items returned might be new or modified.
         * <p>
         * There is a 1:1 relation between input and output iterables.
         * <p>
         * Items are lazily fetched.
         */
        default Iterable<R> iterable(Node<? extends AbstractAlkemyElement<?>> node, Iterable<R> items)
        {
            return new FluentIterable<R>(this, node, items.iterator());
        }

        /**
         * Syntax sugar. See {@link #iterable(Node, Iterable)}
         */
        default Iterable<R> iterable(Node<? extends AbstractAlkemyElement<?>> node, Iterator<R> items)
        {
            return new FluentIterable<R>(this, node, items);
        }

        static class FluentIterable<R> implements Iterable<R>
        {
            private final FluentAlkemyNodeVisitor<R> visitor;
            private final Node<? extends AbstractAlkemyElement<?>> node;
            private final Iterator<R> items;

            FluentIterable(FluentAlkemyNodeVisitor<R> visitor, Node<? extends AbstractAlkemyElement<?>> node, Iterator<R> items)
            {
                this.visitor = visitor;
                this.node = node;
                this.items = items;
            }

            @Override
            public Iterator<R> iterator()
            {
                return new FluentIterator<R>(visitor, node, items);
            }
        }

        static class FluentIterator<R> implements Iterator<R>
        {
            private final FluentAlkemyNodeVisitor<R> visitor;
            private final Node<? extends AbstractAlkemyElement<?>> node;
            private final Iterator<R> items;

            FluentIterator(FluentAlkemyNodeVisitor<R> visitor, Node<? extends AbstractAlkemyElement<?>> node, Iterator<R> items)
            {
                this.visitor = visitor;
                this.node = node;
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
                return visitor.visit(node, items.next());
            }
        }
    }

    /**
     * Returns an iterable of type T mapped from an iterable of type P.
     * <p>
     * There is a 1:1 relation between P and T items.
     * <p>
     * Items are lazily fetched.
     */
    default Iterable<Entry<R, P>> iterable(Node<? extends AbstractAlkemyElement<?>> node, Iterable<P> items, Class<R> retType)
    {
        return new ProcessIterable<R, P>(this, node, items.iterator(), retType);
    }

    /**
     * Syntax sugar. See {@link #iterable(Node, Iterable, Class)}
     */
    default Iterable<Entry<R, P>> iterable(Node<? extends AbstractAlkemyElement<?>> node, Iterator<P> items, Class<R> retType)
    {
        return new ProcessIterable<R, P>(this, node, items, retType);
    }

    /**
     * Returns an iterable of type T.
     * <p>
     * The items are lazily generated on each {@link Iterator#next()} call until the hasNext function returns false.
     */
    default Iterable<R> iterable(Node<? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext, Class<R> retType)
    {
        return new CreateIterable<R, P>(this, node, hasNext, retType);
    }

    static abstract class AbstractIter<R, P>
    {
        protected final AlkemyNodeVisitor<R, P> visitor;
        protected final Node<? extends AbstractAlkemyElement<?>> node;

        protected AbstractIter(AlkemyNodeVisitor<R, P> visitor, Node<? extends AbstractAlkemyElement<?>> node)
        {
            this.visitor = visitor;
            this.node = node;
        }
    }

    static class ProcessIterable<R, P> extends AbstractIter<R, P> implements Iterable<Entry<R, P>>
    {
        private final Iterator<P> items;
        private final Class<R> retType;

        ProcessIterable(AlkemyNodeVisitor<R, P> visitor, Node<? extends AbstractAlkemyElement<?>> node, Iterator<P> items,
                Class<R> retType)
        {
            super(visitor, node);
            this.items = items;
            this.retType = retType;
        }

        @Override
        public Iterator<Entry<R, P>> iterator()
        {
            return new ProcessIterator<R, P>(visitor, node, items, retType);
        }
    }

    static class CreateIterable<R, P> extends AbstractIter<R, P> implements Iterable<R>
    {
        private final Supplier<Boolean> hasNext;
        private final Class<R> retType;

        CreateIterable(AlkemyNodeVisitor<R, P> visitor, Node<? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext,
                Class<R> retType)
        {
            super(visitor, node);
            this.hasNext = hasNext;
            this.retType = retType;
        }

        @Override
        public Iterator<R> iterator()
        {
            return new CreateIterator<R, P>(visitor, node, hasNext, retType);
        }
    }

    static class ProcessIterator<R, P> extends AbstractIter<R, P> implements Iterator<Entry<R, P>>
    {
        private P next;
        private final Iterator<P> items;
        private final Class<R> retType;

        ProcessIterator(AlkemyNodeVisitor<R, P> visitor, Node<? extends AbstractAlkemyElement<?>> node, Iterator<P> items,
                Class<R> retType)
        {
            super(visitor, node);
            this.items = items;
            this.retType = retType;
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
            final R result = visitor.visit(node, next, retType);
            next = items.hasNext() ? items.next() : null;
            return new Entry<R, P>(result, next);
        }
    }

    static class CreateIterator<R, P> extends AbstractIter<R, P> implements Iterator<R>
    {
        private final Supplier<Boolean> hasNext;
        private final Class<R> retType;

        CreateIterator(AlkemyNodeVisitor<R, P> visitor, Node<? extends AbstractAlkemyElement<?>> node, Supplier<Boolean> hasNext,
                Class<R> retType)
        {
            super(visitor, node);
            this.hasNext = hasNext;
            this.retType = retType;
        }

        @Override
        public boolean hasNext()
        {
            return hasNext.get();
        }

        @Override
        public R next()
        {
            return visitor.visit(node, retType);
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
