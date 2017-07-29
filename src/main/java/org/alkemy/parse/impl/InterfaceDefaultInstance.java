package org.alkemy.parse.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO configurable
class InterfaceDefaultInstance
{
    private InterfaceDefaultInstance()
    {
    }

    private final static Map<Class<?>, Class<?>> supported = new HashMap<>();

    static
    {
        supported.put(List.class, ArrayList.class);
        supported.put(Set.class, HashSet.class);
    }

    public static Class<?> get(Class<?> clazz)
    {
        final Class<?> c = supported.get(clazz);
        return c == null ? clazz : c;
    }
}
