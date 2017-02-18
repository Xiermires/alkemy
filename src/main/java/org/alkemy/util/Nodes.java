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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alkemy.util.Node.Builder;

public class Nodes
{
    public static <E> Node.Builder<E> arborescence(E root)
    {
        return new ArborescenceBuilder<>(null, root);
    }

    static class ArborescenceBuilder<E> implements Node.Builder<E>
    {
        private E data;
        private ArborescenceBuilder<E> parent;
        private List<ArborescenceBuilder<E>> children;

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
            return node.drainTo(new ArborescenceNodeImpl<>(), true);
        }

        private Node<E> drainTo(ArborescenceNodeImpl<E> parent, boolean isRoot)
        {
            parent.data = data;
            parent.parent = isRoot ? null : parent;

            if (children != null)
            {
                parent.children = children.stream().map(b -> b.drainTo(new ArborescenceNodeImpl<E>(), false)).collect(Collectors.toList());
            }
            else
            {
                parent.children = Collections.emptyList();
            }
            return parent;
        }
    }

    static class ArborescenceNodeImpl<E> implements Node<E>
    {
        private E data;
        private Node<E> parent;
        private List<Node<E>> children;

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
        public void traverse(Consumer<Node<? extends E>> c)
        {
            traverse(c, p -> true, true);
        }

        /**
         * Consumption order is children first, in order of appearance.
         * <p>
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
        @Override
        public void traverse(Consumer<Node<? extends E>> c, Predicate<? super E> p, boolean keepProcessingOnFailure)
        {
            children().forEach(e ->
            {
                if (p.test(e.data()))
                {
                    c.accept(e);
                    if (e.hasChildren())
                    {
                        e.traverse(c, p, keepProcessingOnFailure);
                    }
                }
                else if (keepProcessingOnFailure && e.hasChildren())
                {
                    e.traverse(c, p, keepProcessingOnFailure);
                }
            });
        }
        
        @Override
        public void traverse(Consumer<Node<? extends E>> onNode, Consumer<Node<? extends E>> onLeaf, Predicate<? super E> p, boolean keepProcessingOnFailure)
        {
            children().forEach(e ->
            {
                if (p.test(e.data()))
                {
                    if (e.hasChildren())
                    {
                        onNode.accept(e);
                        e.traverse(onNode, onLeaf, p, keepProcessingOnFailure);
                    }
                    else
                    {
                        onLeaf.accept(e);
                    }
                }
                else if (keepProcessingOnFailure && e.hasChildren())
                {
                    e.traverse(onNode, onLeaf, p, keepProcessingOnFailure);
                }
            });
        }

        @Override
        public void drainTo(Collection<? super E> c)
        {
            drainTo(c, p -> true, true);
        }

        @Override
        public void drainTo(Collection<? super E> c, Predicate<? super E> p, boolean keepProcessingOnFailure)
        {
            traverse(e -> c.add(e.data()), p, keepProcessingOnFailure);
        }
    }
}
