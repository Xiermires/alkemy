package org.alkemy.functional;

@FunctionalInterface
public interface ObjFloatConsumer<T>
{
    void accept(T t, float value);
}