package org.alkemy.functional;

@FunctionalInterface
public interface ToShortFunction<R>
{
    short apply(R value);
}
