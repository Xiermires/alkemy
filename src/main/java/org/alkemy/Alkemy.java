/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.alkemy;

import static org.alkemy.util.Nodes.arborescence;
import static org.alkemy.util.Nodes.copy;

import java.util.function.Function;
import java.util.function.Predicate;

import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AlkemyElement;
import org.alkemy.parse.impl.AlkemyLeafLexer;
import org.alkemy.parse.impl.AnnotatedAlkemyElementFactory;
import org.alkemy.parse.impl.TypeParser;
import org.alkemy.util.Node;
import org.alkemy.util.Node.Builder;

public class Alkemy
{
    private static final int MAXIMUM_CACHE_SIZE_IN_BYTES = 2 * 1024 * 1024;
    private static final AlkemyParser TYPE_PARSER = TypeParser.create(AlkemyLeafLexer.create(AnnotatedAlkemyElementFactory.create()));

    private static NodeCache cache = new NodeCache(TYPE_PARSER, MAXIMUM_CACHE_SIZE_IN_BYTES);

    private Alkemy()
    {
    }

    private static boolean caching = true;

    /**
     * Enables / Disables parsed nodes caching.
     */
    public static void setCaching(boolean enabled)
    {
        caching = enabled;
    }

    private static long cacheSize = MAXIMUM_CACHE_SIZE_IN_BYTES;

    /**
     * Invalidates the current cache and sets a new max caching size.
     */
    public static void setCacheSize(long newSize)
    {
        cache = new NodeCache(TYPE_PARSER, cacheSize);
    }

    public static Node<AlkemyElement> parse(Class<?> type)
    {
        return caching ? cache.get(type) : TYPE_PARSER.parse(type);
    }

    public static <T extends AlkemyElement> Node<T> parse(Class<?> type//
            , Predicate<AlkemyElement> filter//
            , Function<AlkemyElement, T> function)
    {
        final Node<AlkemyElement> src = parse(type);
        final Builder<T> dst = arborescence(function.apply(src.data()));
        return copy(src, dst, filter, function).build();
    }
}
