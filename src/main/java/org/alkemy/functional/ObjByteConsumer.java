package org.alkemy.functional;

@FunctionalInterface
public interface ObjByteConsumer<T>
{
    void accept(T t, byte value);
}
