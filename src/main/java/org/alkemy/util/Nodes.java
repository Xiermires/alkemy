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
package org.alkemy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// TODO: Change all Node functionality to use the java.util.stream.Node interface.
public class Nodes
{
    public static <E> void drainContentsTo(Node<E> orig, Collection<? super E> dest, Predicate<E> filter)
    {
        traverse(orig, dest, filter);
    }

    public static <E> void traverse(Node<E> orig, Consumer<E> consumer)
    {
        for (Node<E> child : orig.children())
        {
            final E data = child.data();
            consumer.accept(data);
            
            if (!child.children().isEmpty())
            {
                traverse(child, consumer);
            }
        }
    }
    
    private static <E> void traverse(Node<E> orig, Collection<? super E> dest, Predicate<E> filter)
    {
        for (Node<E> child : orig.children())
        {
            final E data = child.data();
            if (filter.test(data))
            {
                dest.add(data);
            }
            
            if (!child.children().isEmpty())
            {
                traverse(child, dest, filter);
            }
        }
    }

    public static <E, T> Node<T> transform(Node<E> orig, Function<E, T> transformer)
    {
        final Node<T> dest = new Node<T>(transformer.apply(orig.data()), null, new ArrayList<Node<T>>());
        transform(orig, dest, transformer);
        return dest;
    }

    private static <E, T> void transform(Node<E> orig, Node<T> dest, Function<E, T> transformer)
    {
        for (final Node<E> node : orig.children())
        {
            final E e = node.data();
            final T t = transformer.apply(e);

            if (node.children().size() == 0)
            {
                dest.children().add(new Node<T>(t, dest, Collections.<Node<T>> emptyList()));
            }
            else
            {
                final Node<T> vertex = new Node<T>(t, dest, new ArrayList<Node<T>>());
                dest.children().add(vertex);
                transform(node, vertex, transformer);
            }
        }
    }
}
