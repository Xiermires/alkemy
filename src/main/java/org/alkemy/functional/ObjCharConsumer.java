package org.alkemy.functional;

@FunctionalInterface
public interface ObjCharConsumer<T>
{
    void accept(T t, char value);
}