/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
