package org.alkemy.functional;

@FunctionalInterface
public interface ToFloatFunction<R>
{
    float apply(R value);
}