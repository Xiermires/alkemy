package org.alkemy.parse.impl;

@FunctionalInterface
public interface NodeConstructorFunction<T>
{
    T newInstance(Object... args);
}
