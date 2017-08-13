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
package org.alkemy.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.alkemy.util.Traversers.Traversable;

public interface Node<E> extends Traversable<E>
{
    Node<E> parent();

    E data();

    List<Node<E>> children();

    boolean hasChildren();

    /**
     * Returns the number of jumps needed to reach from this node to the lowest leaf of this branch.
     * <p>
     * For instance if function returns...
     * <ul>
     * <li>0, then this node is no branch, but a leaf (equivalent to hasChildren() == false).
     * <li>1, then the node has children and all of them are leaves.
     * <li>2, then the node has children and some of them, or all, have children themselves.
     * <li>etc.
     * </ul>
     */
    int branchDepth();

/**
     * As {@link #drainTo(Consumer, Predicate) adding all branch's subnodes.
     * 
     * @param c
     *            result collection
     */
    void drainTo(Collection<? super E> c);

    /**
     * Establish that any {@link #iterator()} created by this node will be pre-order iterator. This is the default behaviour.
     * <p>
     * Equivalent to {@link Traversers#preorder(node)}
     */
    Node<E> preorder();

    /**
     * Establish that any {@link #iterator()} created by this node will be post-order iterator. 
     * <p>
     * Equivalent to {@link Traversers#postorder(node)}
     */
    Node<E> postorder();

    interface Builder<E>
    {
        Builder<E> addChild(E data);

        Node<E> build();
    }
}
