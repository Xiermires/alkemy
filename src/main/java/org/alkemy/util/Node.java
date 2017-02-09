package org.alkemy.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// TODO @FunctionalInterface
public interface Node<E>
{
    Node<E> parent();

    E data();

    List<Node<E>> children();

    void drainTo(Collection<? super E> c);
    
    void drainTo(Collection<? super E> c, Predicate<? super E> p);
    
    void traverse(Consumer<? super E> c);

    void traverse(Consumer<? super E> c, Predicate<? super E> p);
    
    <R> R map(Function<E, R> f); 
    
    interface Builder<E>
    {
        Builder<E> addChild(E data);

        Node<E> build();
    }
}
