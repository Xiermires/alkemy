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
    public enum TraverseStrategy
    {
        PREORDER, POSTORDER
    };

    private Traversers()
    {
    }

    public static <E> Traversable<E> preorder(Node<E> node)
    {
        return new PreorderIterable<E>(node);
    }

    public static <E> Traversable<E> preorder(Node<E> node, Callback<E> callback)
    {
        return new PreorderIterableWithCallback<E>(node, callback);
    }

    public static <E> Traversable<E> postorder(Node<E> node)
    {
        return new PostorderIterable<E>(node);
    }

    public static <E> Traversable<E> postorder(Node<E> node, Callback<E> callback)
    {
        return new PostorderIterableWithCallback<E>(node, callback);
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

    static class PreorderIterableWithCallback<E> implements Traversable<E>
    {
        private final Node<E> node;
        private final Callback<E> callback;

        PreorderIterableWithCallback(Node<E> node, Callback<E> callback)
        {
            this.node = node;
            this.callback = callback;
        }

        @Override
        public Iterator<E> iterator()
        {
            return new PreorderIteratorWithCallback<E>(node, callback);
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

    static class PostorderIterableWithCallback<E> implements Traversable<E>
    {
        private final Node<E> node;
        private final Callback<E> callback;

        PostorderIterableWithCallback(Node<E> node, Callback<E> callback)
        {
            this.node = node;
            this.callback = callback;
        }

        @Override
        public Iterator<E> iterator()
        {
            return new PostorderIteratorWithCallback<E>(node, callback);
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

    static class PreorderIteratorWithCallback<E> implements Iterator<E>
    {
        private final Deque<Pair<Node<E>, Iterator<Node<E>>>> stack = new ArrayDeque<>();
        private final Deque<Node<E>> exit = new ArrayDeque<>();
        private final Callback<E> callback;

        private Iterator<Node<E>> currentChildren = null;
        private Node<E> currentParent = null;
        private Node<E> next = null;

        boolean onEnter = false;

        PreorderIteratorWithCallback(Node<E> node, Callback<E> callback)
        {
            this.next = node;
            this.callback = callback;
            if (next.hasChildren())
            {
                onEnter = true;
                currentParent = next;
                currentChildren = next.children().iterator();
            }
        }

        @Override
        public boolean hasNext()
        {
            if (next == null)
            {
                doOnExit();
                return false;
            }
            return true;
        }

        @Override
        public E next()
        {
            doOnEnter();
            doOnExit();

            final E current = next.data();
            advance();
            return current;
        }

        void doOnEnter()
        {
            if (onEnter)
            {
                callback.onEnterNode(next);
                onEnter = false;
            }
        }

        void doOnExit()
        {
            while (!exit.isEmpty())
            {
                callback.onExitNode(exit.pop());
            }
        }

        void advance()
        {
            if (currentChildren.hasNext())
            {
                next = currentChildren.next();
                if (next.hasChildren())
                {
                    stack.push(Pair.create(currentParent, currentChildren));
                    currentChildren = next.children().iterator();
                    currentParent = next;
                    onEnter = true;
                }
            }
            else
            {
                exit.push(currentParent);
                reverse();
            }
        }

        void reverse()
        {
            if (!stack.isEmpty())
            {
                final Pair<Node<E>, Iterator<Node<E>>> pop = stack.pop();
                currentParent = pop.first;
                currentChildren = pop.second;
                advance();
            }
            else
            {
                next = null;
            }
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

    static class PostorderIteratorWithCallback<E> implements Iterator<E>
    {
        private final Deque<Object> stack = new ArrayDeque<>();

        private Iterator<Node<E>> currentChildren = null;
        private final Callback<E> callback;
        private Node<E> next = null;
        private boolean init = false;

        PostorderIteratorWithCallback(Node<E> node, Callback<E> callback)
        {
            this.next = node;
            this.callback = callback;
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        void init()
        {
            if (next.hasChildren())
            {
                currentChildren = next.children().iterator();
                stack.push(next);
                stack.push(currentChildren);
                next = currentChildren.next();
                advanceUntilLeaf();
            }
        }

        @Override
        public E next()
        {
            if (!init)
                init();

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
                callback.onEnterNode(next);
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
                        callback.onExitNode(next);
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

    public static interface Callback<E>
    {
        boolean onEnterNode(Node<E> node);

        boolean onExitNode(Node<E> node);
    }
}
