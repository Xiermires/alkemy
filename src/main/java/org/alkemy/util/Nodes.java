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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alkemy.AlkemyNodes;
import org.alkemy.parse.impl.AlkemyElement;
import org.alkemy.util.Node.Builder;
import org.alkemy.util.Traversers.PostorderIterator;
import org.alkemy.util.Traversers.PreorderIterator;

public class Nodes
{
    public static <E> Node.Builder<E> arborescence(E root)
    {
        return new ArborescenceBuilder<>(null, root);
    }

    public static <E, T> Builder<T> copy(Node<E> orig, Builder<T> dest, Predicate<? super E> p, Function<E, T> f)
    {
        orig.children().stream() //
                .filter(filter -> p.test(filter.data())).forEach(e -> copy(e, dest.addChild(f.apply(e.data())), p, f));
        return dest;
    }

    static class ArborescenceBuilder<E> implements Node.Builder<E>
    {
        private E data;
        private ArborescenceBuilder<E> parent;
        private List<ArborescenceBuilder<E>> children;
        private int depth;

        ArborescenceBuilder(ArborescenceBuilder<E> parent, E data)
        {
            this.data = data;
            this.parent = parent;
        }

        @Override
        public Builder<E> addChild(E data)
        {
            if (children == null)
            {
                children = new ArrayList<ArborescenceBuilder<E>>();
            }
            final ArborescenceBuilder<E> child = new ArborescenceBuilder<>(this, data);
            this.children.add(child);
            return child;
        }

        @Override
        public Node<E> build()
        {
            ArborescenceBuilder<E> node = this;
            while (node.parent != null)
            {
                node = node.parent;
            }
            return calculateDepths(node).drainTo(new ArborescenceNode<>(), true);
        }

        private ArborescenceBuilder<E> calculateDepths(ArborescenceBuilder<E> e)
        {
            updateDepth(e, new IntRef());
            return e;
        }

        private ArborescenceBuilder<E> updateDepth(ArborescenceBuilder<E> e, IntRef ir)
        {
            if (e.children != null)
            {
                for (int i = 0; i < e.children.size(); i++)
                {
                    IntRef ir2 = new IntRef();
                    updateDepth(e.children.get(i), ir2);
                    ir2.i++;
                    ir.i = Math.max(ir.i, ir2.i);
                    e.depth = ir.i;
                }
            }
            else
            {
                e.depth = 0;
            }
            return e;
        }

        // TODO: Probably bring this inside the calculateDepths.
        // We need new IntRefs for each element in the for each and access them after every
        // iteration.
        private Node<E> drainTo(ArborescenceNode<E> parent, boolean isRoot)
        {
            parent.data = data;
            parent.parent = isRoot ? null : parent;
            if (children != null)
            {
                parent.children = children.stream()//
                        .map(b -> b.drainTo(new ArborescenceNode<E>(), false)).collect(Collectors.toList());
                parent.depth = depth;
            }
            else
            {
                parent.children = Collections.emptyList();
                parent.depth = depth;
            }
            return parent;
        }
    }

    static class IntRef
    {
        int i;
    }

    static class ArborescenceNode<E> implements Node<E>
    {
        private enum TraverseStrategy
        {
            PREORDER, POSTORDER
        };

        private E data;
        private Node<E> parent;
        private List<Node<E>> children;
        private int depth;
        private TraverseStrategy traverseStrategy = TraverseStrategy.PREORDER;

        ArborescenceNode(E data, Node<E> parent, List<Node<E>> children, int depth)
        {
            this.data = data;
            this.parent = parent;
            this.children = children;
            this.depth = depth;
        }
        
        ArborescenceNode()
        {
        }
        
        @Override
        public Iterator<E> iterator()
        {
            return traverseStrategy == TraverseStrategy.PREORDER ? new PreorderIterator<E>(this) : new PostorderIterator<E>(this);
        }

        @Override
        public Node<E> parent()
        {
            return parent;
        }

        @Override
        public E data()
        {
            return data;
        }

        @Override
        public List<Node<E>> children()
        {
            return children;
        }

        @Override
        public boolean hasChildren()
        {
            return !children.isEmpty();
        }

        @Override
        public int branchDepth()
        {
            return depth;
        }

        @Override
        public void drainTo(Collection<? super E> c)
        {
            forEach(e -> c.add(e));
        }

        @Override
        public Node<E> preorder()
        {
            traverseStrategy = TraverseStrategy.PREORDER;
            return this;
        }

        @Override
        public Node<E> postorder()
        {
            traverseStrategy = TraverseStrategy.POSTORDER;
            return this;
        }
    }

    public static class RootNode<R, E> extends ArborescenceNode<E>
    {
        private Class<R> type;

        public RootNode(Node<E> root, Class<R> type)
        {
            super(root.data(), null, root.children(), root.branchDepth());
            this.type = type;
        }

        public static <R> RootNode<R, AlkemyElement> create(Class<R> type)
        {
            return new RootNode<R, AlkemyElement>(AlkemyNodes.get(type), type);
        }

        public static <R, E> RootNode<R, E> create(Node<E> root, Class<R> type)
        {
            return new RootNode<R, E>(root, type);
        }

        public static <R, E> RootNode<R, E> create(Class<R> type, Function<AlkemyElement, E> f)
        {
            final Node<AlkemyElement> src = AlkemyNodes.get(type);
            final Builder<E> dst = arborescence(f.apply(src.data()));
            final Node<E> copy = copy(src, dst, p -> true, f).build();
            return new RootNode<R, E>(copy, type);
        }

        public Class<R> type()
        {
            return type;
        }
    }
}
