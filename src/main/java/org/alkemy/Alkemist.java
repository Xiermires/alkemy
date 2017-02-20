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
import java.util.function.Supplier;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Assertions;
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
     * Parses the type of T in search of Alkemizations and delegates them to the included visitors.
     */
    public <T> T process(T t)
    {
        Assertions.exists(t);
        anv.visit(cache.get(t.getClass()), t);
        return t;
    }

    /**
     * Parses the type of T in search of Alkemizations and creates an instance of type T, appropriately filled by the supported visitors.
     */
    public <T> T create(Class<T> clazz)
    {
        Assertions.existAll(clazz, anv);
        return clazz.cast(anv.visit(cache.get(clazz)));
    }

    public <T> Iterable<T> iterable(Iterable<T> items)
    {
        return new AlkemistIterable<T>(this, items.iterator());
    }
    
    public <T> Iterable<T> iterable(Class<T> type, Supplier<Boolean> hasNext)
    {
        return new AlkemistIterable<T>(this, type, hasNext);
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
     * See {@link #create(Class)}
     */
    public static <T> T process(T t, AlkemyNodeVisitor anv, AlkemyParser parser)
    {
        Assertions.existAll(t, anv);
        anv.visit(parser.parse(t.getClass()), t);
        return t;
    }
    
    static class AlkemistIterable<T> implements Iterable<T>
    {
        enum Mode { PROCESS, CREATE };
        
        private final Alkemist theAlkemist;
        private final Iterator<T> theItems;
        private final Class<T> theType;
        private final Supplier<Boolean> theEnd;
        
        private final Mode mode;
        
        AlkemistIterable(Alkemist alkmst, Iterator<T> items)
        {
            theAlkemist = alkmst;
            theItems = items;
            theType = null;
            theEnd = null;
            
            mode = Mode.PROCESS;
        }
        
        AlkemistIterable(Alkemist alkmst, Class<T> type, Supplier<Boolean> hasNext)
        {
            theAlkemist = alkmst;
            theItems = null;
            theType = type;
            theEnd = hasNext;
            
            mode = Mode.CREATE;
        }
        
        @Override
        public Iterator<T> iterator()
        {
            switch (mode)
            {
                case PROCESS:
                    return new AlkemistProcessIterator<T>(theAlkemist, theItems);
                case CREATE:
                    return new AlkemistCreateIterator<T>(theAlkemist, theType, theEnd);
            }
            throw new AlkemyException("Undefined enum '%d'", mode.name()); // should never happen
        }
    }
    
    static class AlkemistProcessIterator<T> implements Iterator<T>
    {
        private final Alkemist theAlkemist;
        private final Iterator<T> theItems;
        
        AlkemistProcessIterator(Alkemist alkmst, Iterator<T> items)
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
    
    static class AlkemistCreateIterator<T> implements Iterator<T>
    {
        private final Alkemist theAlkemist;
        private final Class<T> theType;
        private final Supplier<Boolean> theEnd;
        
        AlkemistCreateIterator(Alkemist alkmst, Class<T> type, Supplier<Boolean> hasNext)
        {
            theAlkemist = alkmst;
            theType = type;
            theEnd = hasNext;
        }
        
        @Override
        public boolean hasNext()
        {
            return theEnd.get();
        }

        @Override
        public T next()
        {
            final T t = theAlkemist.create(theType);
            return t;
        }
    }
}
