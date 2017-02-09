package org.alkemy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alkemy.util.Node.Builder;

public class Nodes
{
    public static <E> Node.Builder<E> arborescence(E root)
    {
        return new ArberescenceBuilder<>(null, root);
    }

    static class ArberescenceBuilder<E> implements Node.Builder<E>
    {
        private E data;
        private ArberescenceBuilder<E> parent;
        private List<ArberescenceBuilder<E>> children;

        ArberescenceBuilder(ArberescenceBuilder<E> parent, E data)
        {
            this.data = data;
            this.parent = parent;
        }

        @Override
        public Builder<E> addChild(E data)
        {
            if (children == null)
            {
                children = new ArrayList<ArberescenceBuilder<E>>();
            }
            final ArberescenceBuilder<E> child = new ArberescenceBuilder<>(this, data);
            this.children.add(child);
            return child;
        }

        @Override
        public Node<E> build()
        {
            ArberescenceBuilder<E> node = this;
            while (node.parent != null)
            {
                node = node.parent;
            }
            return node.drainTo(new NodeImpl<>(), true);
        }

        private Node<E> drainTo(NodeImpl<E> parent, boolean isRoot)
        {
            parent.data = data;
            parent.parent = isRoot ? null : parent;

            if (children != null)
            {
                parent.children = children.stream().map(b -> b.drainTo(new NodeImpl<E>(), false)).collect(Collectors.toList());
            }
            else
            { 
                parent.children = Collections.emptyList();
            }
            return parent;
        }
    }

    static class NodeImpl<E> implements Node<E>
    {
        private E data;
        private Node<E> parent;
        private List<Node<E>> children;

        public Node<E> parent()
        {
            return parent;
        }

        public E data()
        {
            return data;
        }

        public List<Node<E>> children()
        {
            return children;
        }

        @Override
        public void drainTo(Collection<? super E> c)
        {
            traverse(e -> c.add(e), p -> true);
        }
        
        @Override
        public void drainTo(Collection<? super E> c, Predicate<? super E> p)
        {
            traverse(e -> c.add(e));
        }

        @Override
        public void traverse(Consumer<? super E> c)
        {
            children().forEach(e -> process(e, c, p -> true));
        }
        
        @Override
        public void traverse(Consumer<? super E> c, Predicate<? super E> p)
        {
            children().forEach(e -> process(e, c, p));
        }

        private void process(Node<E> e, Consumer<? super E> c, Predicate<? super E> p)
        {
            if (p.test(e.data()))
            {
                c.accept(e.data());
            }
            
            if (!e.children().isEmpty())
            {
                e.children().forEach(ec -> process(ec, c, p));
            }
        }

        @Override
        public <R> R map(Function<E, R> f)
        {
            return f.apply(data);
        }
    }
}
