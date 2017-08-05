package org.alkemy.functional;

@FunctionalInterface
public interface ObjBooleanConsumer<T>
{
    void accept(T t, boolean value);
}