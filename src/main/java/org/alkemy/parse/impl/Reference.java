package org.alkemy.parse.impl;

import java.lang.reflect.Field;
import java.util.Collection;

import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.ValueAccessor;
import org.alkemy.util.Types;

public class Reference implements ValueAccessor
{
    private final Class<?> type;
    private final String name;
    private final boolean collection;
    private final Class<?> componentType;

    // not a field
    Reference()
    {
        this.type = null;
        this.name = null;
        this.collection = false;
        this.componentType = null;
    }

    Reference(Field f)
    {
        this.name = f.getDeclaringClass().getTypeName() + "." + f.getName();
        this.type = f.getType();
        this.componentType = Types.getComponentType(f);
        this.collection = Collection.class.isAssignableFrom(type);
    }

    @Override
    public Class<?> type()
    {
        return type;
    }

    @Override
    public boolean isCollection()
    {
        return collection;
    }

    @Override
    public Class<?> componentType()
    {
        return componentType;
    }

    @Override
    public String valueName()
    {
        return name;
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void set(Object value, Object parent) throws AlkemyException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
