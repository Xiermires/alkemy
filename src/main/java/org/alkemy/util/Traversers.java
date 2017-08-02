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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Traversers
{
    private Traversers()
    {
    }

    public static <E> Traversable<E> preorder(Node<E> node)
    {
        return new PreorderIterable<E>(node);
    }

    public static <E> Traversable<E> postorder(Node<E> node)
    {
        return new PostorderIterable<E>(node);
    }

    public static interface Traversable<E> extends Iterable<E>
    {
        default Stream<E> stream()
        {
            return StreamSupport.stream(spliterator(), false);
        }
    }

    static class PreorderIterable<E> implements Traversable<E>
    {
        private final Node<E> node;

        PreorderIterable(Node<E> node)
        {
            this.node = node;
        }

        @Override
        public Iterator<E> iterator()
        {
            return new PreorderIterator<E>(node);
        }
    }
    
    static class PreorderIterator<E> implements Iterator<E>
    {
        private final Deque<Iterator<Node<E>>> stack = new ArrayDeque<Iterator<Node<E>>>();

        private Iterator<Node<E>> currentChildren = null;
        private Node<E> next = null;

        PreorderIterator(Node<E> node)
        {
            this.next = node;
            if (next.hasChildren())
            {
                currentChildren = next.children().iterator();
            }
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        @Override
        public E next()
        {
            final E current = next.data();
            advance();
            return current;
        }

        void advance()
        {
            if (currentChildren.hasNext())
            {
                next = currentChildren.next();
                if (next.hasChildren())
                {
                    stack.push(currentChildren);
                    currentChildren = next.children().iterator();
                }
            }
            else
            {
                reverse();
            }
        }

        void reverse()
        {
            if (!stack.isEmpty())
            {
                currentChildren = stack.pop();
                advance();
            }
            else
            {
                next = null;
            }
        }
    }

    static class PostorderIterable<E> implements Traversable<E>
    {
        private final Node<E> node;

        PostorderIterable(Node<E> node)
        {
            this.node = node;
        }

        @Override
        public Iterator<E> iterator()
        {
            return new PostorderIterator<E>(node);
        }
    }

    static class PostorderIterator<E> implements Iterator<E>
    {
        private final Deque<Object> stack = new ArrayDeque<>();

        private Iterator<Node<E>> currentChildren = null;
        private Node<E> next = null;

        PostorderIterator(Node<E> node)
        {
            next = node;
            if (node.hasChildren())
            {
                currentChildren = node.children().iterator();
                stack.push(next);
                stack.push(currentChildren);
                next = currentChildren.next();
                advanceUntilLeaf();
            }
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        @Override
        public E next()
        {
            final E current = next.data();
            if (currentChildren.hasNext())
            {
                next = currentChildren.next();
                advanceUntilLeaf();
            }
            else
            {
                reverseUntilLeaf();
            }
            return current;
        }

        void advanceUntilLeaf()
        {
            if (next.hasChildren())
            {
                stack.push(currentChildren);
                stack.push(next);
                currentChildren = next.children().iterator();
                next = currentChildren.next();
                advanceUntilLeaf();
            }
        }

        @SuppressWarnings("unchecked")
        void reverseUntilLeaf()
        {
            if (!stack.isEmpty())
            {
                final Object x = stack.pop();
                if (x instanceof Iterator)
                {
                    currentChildren = (Iterator<Node<E>>) x;
                    if (currentChildren.hasNext())
                    {
                        next = currentChildren.next();
                        advanceUntilLeaf();
                    }
                    else
                    {
                        reverseUntilLeaf();
                    }
                }
                else
                {
                    next = (Node<E>) x;
                }
            }
            else
            {
                next = null;
            }
        }
    }
}
