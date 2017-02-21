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
package org.alkemy;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Assertions;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor;

/**
 * The Alkemist class allows applying user specific {@link AlkemyNodeVisitor} and {@link AlkemyElementVisitor} strategies to a set
 * of alkemy elements.
 * <p>
 * Alkemy elements are the result of parsing an "alkemized" type with an {@link AlkemyParser}, being "alkemized" user defined.
 * <p>
 * For instance the {@link AlkemyParsers#fieldParser()} searches fields "super" annotated as {@link AlkemyLeaf} within the type
 * hierarchy and groups them as a directed rooted tree starting from the parsed type.
 * <p>
 * An Alkemist can be reused for as many types as needed.
 * <p>
 * If a type define alkemizations which are not supported by the loaded visitors, those alkemy elements are not processed.
 */
public class Alkemist
{
    private AlkemyLoadingCache cache;
    private AlkemyNodeVisitor anv;

    Alkemist(AlkemyLoadingCache cache, AlkemyNodeVisitor anv)
    {
        Assertions.existAll(cache, anv);

        this.cache = cache;
        this.anv = anv;
    }

    /**
     * Parses the type of T in search of Alkemizations and delegates its processing to the underlying visitors.
     */
    public <T> T process(T t)
    {
        Assertions.exists(t);
        anv.visit(cache.get(t.getClass()), t);
        return t;
    }
    
    /**
     * As {@link #process(Object)} but including parameters.
     */
    public <T> T process(T t, Object... args)
    {
        Assertions.exists(t);
        anv.visit(cache.get(t.getClass()), t, args);
        return t;
    }

    /**
     * Parses the type of T in search of Alkemizations, creates an instance of type T and delegates its processing to the
     * underlying visitors.
     */
    public <T> T process(Class<T> clazz)
    {
        Assertions.existAll(clazz, anv);
        final Node<? extends AbstractAlkemyElement<?>> node = cache.get(clazz);
        return clazz.cast(anv.visit(node, node.data().newInstance()));
    }

    /**
     * As {@link #process(Class)} but including parameters.
     */
    public <T> T process(Class<T> clazz, Object... args)
    {
        Assertions.existAll(clazz, args);
        final Node<? extends AbstractAlkemyElement<?>> node = cache.get(clazz);
        return clazz.cast(anv.visit(node, node.data().newInstance(), args));
    }

    /**
     * Delegates any decision regarding the class to this Alkemist {@link AlkemyNodeVisitor}.
     */
    public <T> T delegateToNodeVisitor(Class<T> clazz)
    {
        Assertions.existAll(clazz, anv);
        return clazz.cast(anv.visit(cache.get(clazz)));
    }

    /**
     * As {@link #delegateToNodeVisitor(Class)} but including parameters.
     */
    public <T> T delegateToNodeVisitor(Class<T> clazz, Object... args)
    {
        Assertions.existAll(clazz, args);
        return clazz.cast(anv.visit(cache.get(clazz), args));
    }
    
    /**
     * Returns an iterable of type R mapped from a collection of items of type T.
     * <p>
     * There is a 1:1 relation between T and R items.
     * <p>
     * Items are lazily fetched and the before consumer is called always before the mapping proceeds.
     */
    public <T, R> Iterable<R> iterable(Class<R> type, Consumer<T> before, Iterable<T> items)
    {
        return new MapIterable<T, R>(this, type, before, items.iterator());
    }

    /**
     * Returns an iterable of type T. The items in the returned iterable might be new, or the provided items modified.
     * <p>
     * There is a 1:1 relation between input and output iterables.
     * <p>
     * Items are lazily fetched.
     */
    public <T> Iterable<T> iterable(Iterable<T> items)
    {
        return new ProcessIterable<T>(this, items.iterator());
    }

    /**
     * Returns an iterable of type T. The items are lazily generated on each {@link Iterator#next()} call until the hasNext function returns false.
     */
    public <T> Iterable<T> iterable(Class<T> type, Supplier<Boolean> hasNext)
    {
        return new CreateIterable<T>(this, type, hasNext);
    }

    /**
     * See {@link #iterable(Class, Consumer, Iterable)}
     */
    public <T, R> Iterable<R> iterable(Class<R> type, Consumer<T> before, Iterator<T> items)
    {
        return new MapIterable<T, R>(this, type, before, items);
    }

    /**
     * See {@link #iterable(Iterable)}
     */
    public <T> Iterable<T> iterable(Iterator<T> items)
    {
        return new ProcessIterable<T>(this, items);
    }

    /**
     * Syntax sugar to avoid the {@link AlkemistBuilder} syntax overhead.
     * <p>
     * This method doesn't use any caching system and shouldn't be used for repeating tasks.
     * <p>
     * See {@link #process(Object)}
     */
    public static <T> T create(Class<T> clazz, AlkemyNodeVisitor anv, AlkemyParser parser)
    {
        Assertions.existAll(clazz, anv);
        return clazz.cast(anv.visit(parser.parse(clazz)));
    }

    /**
     * Syntax sugar to avoid the {@link AlkemistBuilder} syntax overhead.
     * <p>
     * This method doesn't use any caching system and shouldn't be used for repeating tasks.
     * <p>
     * See {@link #process(Class)}
     */
    public static <T> T process(T t, AlkemyNodeVisitor anv, AlkemyParser parser)
    {
        Assertions.existAll(t, anv);
        anv.visit(parser.parse(t.getClass()), t);
        return t;
    }

    static class ProcessIterable<T> implements Iterable<T>
    {
        private final Alkemist alkemist;
        private final Iterator<T> items;

        ProcessIterable(Alkemist alkemist, Iterator<T> items)
        {
            this.alkemist = alkemist;
            this.items = items;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new ProcessIterator<T>(alkemist, items);
        }
    }

    static class CreateIterable<T> implements Iterable<T>
    {
        private final Alkemist alkemist;
        private final Class<T> type;
        private final Supplier<Boolean> hasNext;

        CreateIterable(Alkemist alkemist, Class<T> type, Supplier<Boolean> hasNext)
        {
            this.alkemist = alkemist;
            this.type = type;
            this.hasNext = hasNext;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new CreateIterator<T>(alkemist, type, hasNext);
        }
    }

    static class MapIterable<T, R> implements Iterable<R>
    {
        private final Alkemist alkemist;
        private final Class<R> type;
        private final Consumer<T> before;
        private final Iterator<T> items;

        MapIterable(Alkemist alkemist, Class<R> type, Consumer<T> before, Iterator<T> items)
        {
            this.alkemist = alkemist;
            this.type = type;
            this.before = before;
            this.items = items;
        }

        @Override
        public Iterator<R> iterator()
        {
            return new MapIterator<T, R>(alkemist, type, before, items);
        }
    }

    static class ProcessIterator<T> implements Iterator<T>
    {
        private final Alkemist theAlkemist;
        private final Iterator<T> theItems;

        ProcessIterator(Alkemist alkmst, Iterator<T> items)
        {
            theAlkemist = alkmst;
            theItems = items;
        }

        @Override
        public boolean hasNext()
        {
            return theItems.hasNext();
        }

        @Override
        public T next()
        {
            return theAlkemist.process(theItems.next());
        }
    }

    static class CreateIterator<T> implements Iterator<T>
    {
        private final Alkemist alkemist;
        private final Class<T> type;
        private final Supplier<Boolean> hasNext;

        CreateIterator(Alkemist alkemist, Class<T> type, Supplier<Boolean> hasNext)
        {
            this.alkemist = alkemist;
            this.type = type;
            this.hasNext = hasNext;
        }

        @Override
        public boolean hasNext()
        {
            return hasNext.get();
        }

        @Override
        public T next()
        {
            final T t = alkemist.process(type);
            return t;
        }
    }

    static class MapIterator<T, R> implements Iterator<R>
    {
        private final Alkemist alkemist;
        private final Class<R> type;
        private final Consumer<T> before;
        private final Iterator<T> items;

        MapIterator(Alkemist alkemist, Class<R> type, Consumer<T> before, Iterator<T> items)
        {
            this.alkemist = alkemist;
            this.type = type;
            this.before = before;
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
            final T t = items.next();
            before.accept(t);
            return alkemist.process(type);
        }
    }
}
