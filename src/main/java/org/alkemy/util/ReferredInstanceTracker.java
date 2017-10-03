package org.alkemy.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import org.alkemy.parse.impl.AlkemyElement;
import org.alkemy.util.Traversers.Callback;

public class ReferredInstanceTracker<E extends AlkemyElement> implements Callback<E>, Supplier<Object>
{
    private Object ref = null;
    private final Deque<Object> instances = new ArrayDeque<>();

    public ReferredInstanceTracker(Object o)
    {
        ref = o;
    }

    @Override
    public boolean onEnterNode(Node<E> node)
    {
        final Object instance = node.data().get(ref);
        if (instance != null)
        {
            instances.push(ref);
            ref = instance;
        }
        return instance != null;
    }

    @Override
    public boolean onExitNode(Node<E> node)
    {
        if (!instances.isEmpty())
            ref = instances.pop();

        return true;
    }

    @Override
    public Object get()
    {
        return ref;
    }
}
