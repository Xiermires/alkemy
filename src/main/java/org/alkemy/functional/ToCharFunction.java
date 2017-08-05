package org.alkemy.functional;

@FunctionalInterface
public interface ToCharFunction<R>
{
    char apply(R value);
}