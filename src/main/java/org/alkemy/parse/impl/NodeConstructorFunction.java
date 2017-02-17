package org.alkemy.parse.impl;

@FunctionalInterface
public interface NodeConstructorFunction
{
    Object newInstance(Object... args);
}
