package org.alkemy.functional;

@FunctionalInterface
public interface ObjShortConsumer<T>
{
    void accept(T t, short value);
}
