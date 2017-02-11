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

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

// TODO @FunctionalInterface
public interface Node<E>
{
    Node<E> parent();

    E data();

    List<Node<E>> children();

    boolean hasChildren();

    /**
     * As {@link #traverse(Consumer, Predicate) consuming all branches nodes.
     * 
     * @param c node consumer
     */
    void traverse(Consumer<Node<? extends E>> c);

    /**
     * Applies c to every node in the underlying branches satisfying p. 
     * <p>
     * Consumption order is children first, in order of appearence.
     * <p>
     * Depending on <code>keepProcessingOnFailure</code>, children nodes of a node failing p are processed or not. 
     * <p>
     * <ul>
     * <li>If <code>keepProcessingOnFailure</code> is <em>true</em>, the offspring of a failed node will still test p and be potentially consumed.
     * <li>If <code>keepProcessingOnFailure</code> is <em>false</em>, the offspring of a failed node are ignored and won't be consumed.
     * </ul>
     * Example: children = { c1 = { c1.c1, c1.c2, c1.c3 }, c2, c3 = { c3.c1 } }, test(c1.c2) & test(c3) fail, rest succeed. 
     * <p>
     * <code>keepProcessingOnFailure = true</code>
     * <ol>
     * <li>c1
     * <ol>
     * <li>c1.c1
     * <li><del>c1.c2</del>
     * <li>c1.c3
     * </ol>
     * <li>c2
     * <li><del>c3</del>
     * <ol>
     * <li>c3.c1
     * </ol>
     * </ol>
     * <code>keepProcessingOnFailure = false</code>
     * <ol>
     * <li>c1
     * <ol>
     * <li>c1.c1
     * <li><del>c1.c2</del>
     * <li>c1.c3
     * </ol>
     * <li>c2
     * <li><del>c3</del>
     * <ol>
     * <li><del>c3.c1</del>
     * </ol>
     * </ol>
     * 
     * @param c
     *            node consumer
     * @param p
     *            consumption condition
     * @param keepProcessingOnFailure
     *            process subnodes of a node failing p.
     */
    void traverse(Consumer<Node<? extends E>> c, Predicate<? super E> p, boolean keepProcessingOnFailure);

    /**
     * As {@link #drainTo(Consumer, Predicate) adding all branch's subnodes.
     * 
     * @param c
     *            result collection
     */
    void drainTo(Collection<? super E> c);

    /**
     * Adds every branch's subnodes data satisfying p to the collection.
     * 
     * @param c
     *            result collection
     * @param p
     *            addition condition
     * @param keepProcessingOnFailure
     *            process subnodes of a node failing p (see {@link #traverse(Consumer, Predicate, boolean)} for additional info)
     */
    void drainTo(Collection<? super E> c, Predicate<? super E> p, boolean keepProcessingOnFailure);

    static <E> Builder<E> copy(Node<E> orig, Builder<E> dest)
    {
        orig.children().forEach(e -> copy(e, dest.addChild(e.data())));
        return dest;
    }

    interface Builder<E>
    {
        Builder<E> addChild(E data);

        Node<E> build();
    }
}
