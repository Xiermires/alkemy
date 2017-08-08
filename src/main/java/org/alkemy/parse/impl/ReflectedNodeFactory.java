/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
package org.alkemy.parse.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.AutoCastValueAccessor;
import org.alkemy.parse.InterfaceDefaultInstance;
import org.alkemy.parse.NodeFactory;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

public class ReflectedNodeFactory implements NodeFactory
{
    private final Supplier<Object> typeCtor;
    private final Supplier<Object> componentTypeCtor;
    private final AutoCastValueAccessor valueAccessor;

    ReflectedNodeFactory(AutoCastValueAccessor valueAccessor)
    {
        this.typeCtor = getCtor(InterfaceDefaultInstance.get(valueAccessor.type()));
        this.componentTypeCtor = valueAccessor.componentType() != null ? //
        getCtor(InterfaceDefaultInstance.get(valueAccessor.componentType()))
                : null;
        this.valueAccessor = valueAccessor;
    }

    Supplier<Object> getCtor(Class<?> type)
    {
        try
        {
            final Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return () -> _newInstance(ctor);
        }
        catch (NoSuchMethodException e)
        {
            // default values are ignored. any alternatives ?
            final Objenesis objenesis = new ObjenesisStd();
            final ObjectInstantiator<?> instantiator = objenesis.getInstantiatorOf(type);
            return () -> instantiator.newInstance();
        }
    }

    Object _newInstance(Constructor<?> ctor) throws AlkemyException {
        try
        {
            return ctor.newInstance();
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new AlkemyException("Unable to instantiate type '%s'", e, ctor.getName());
        }
    }
    
    @Override
    public Object newInstance(Object... unused) throws AlkemyException
    {
        return typeCtor.get();
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return valueAccessor.type();
    }

    @Override
    public Class<?> componentType() throws AlkemyException
    {
        return valueAccessor.componentType();
    }

    @Override
    public Object newComponentInstance(Object... unused) throws AlkemyException
    {
        return componentTypeCtor.get();
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        return valueAccessor.get(parent);
    }

    @Override
    public void set(Object value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public String valueName()
    {
        return valueAccessor.valueName();
    }

    @Override
    public boolean isCollection()
    {
        return valueAccessor.isCollection();
    }
}
