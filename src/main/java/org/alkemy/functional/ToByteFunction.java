package org.alkemy.functional;

@FunctionalInterface
public interface ToByteFunction<R>
{
    byte apply(R value);
}