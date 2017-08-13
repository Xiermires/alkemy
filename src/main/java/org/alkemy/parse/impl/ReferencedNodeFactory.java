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

import java.util.Arrays;
import java.util.function.Supplier;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.AutoCastValueAccessor;
import org.alkemy.parse.ConstructorFunction;
import org.alkemy.parse.NodeFactory;
import org.alkemy.util.Assertions;

public class ReferencedNodeFactory implements NodeFactory
{
    private final Supplier<?> noargsCtor;
    private final Supplier<?> noargsComponentCtor;
    private final ConstructorFunction staticFactory;
    private AutoCastValueAccessor valueAccessor;

    ReferencedNodeFactory(Supplier<?> noargsCtor, Supplier<?> noargsComponentCtor, ConstructorFunction staticFactory,
            AutoCastValueAccessor valueAccessor)
    {
        Assertions.noneNull(noargsCtor, staticFactory);

        this.noargsCtor = noargsCtor;
        this.noargsComponentCtor = noargsComponentCtor;
        this.staticFactory = staticFactory;
        this.valueAccessor = valueAccessor;
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return valueAccessor.type();
    }

    @Override
    public Object newInstance(Object... args) throws AlkemyException
    {
        try
        {
            if (args.length == 0)
            {
                return noargsCtor.get();
            }
            else if (!valueAccessor.isCollection())
            {
                return staticFactory.newInstance(args);
            }
            else return null;
        }
        catch (Throwable e)
        {
            throw new AccessException("Provided arguments '%s' do not match the ctor expected arguments of type '%s'.", e, Arrays
                    .asList(args), type());
        }
    }

    @Override
    public Class<?> componentType() throws AlkemyException
    {
        return valueAccessor.componentType();
    }

    @Override
    public Object newComponentInstance(Object... args) throws AlkemyException
    {
        try
        {
            if (args.length == 0)
            {
                return noargsComponentCtor.get();
            }
            else if (valueAccessor.isCollection())
            {
                return staticFactory.newInstance(args);
            }
            else return null;
        }
        catch (Throwable e)
        {
            throw new AccessException("Provided arguments '%s' do not match the ctor expected arguments of type '%s'.", e, Arrays
                    .asList(args), type());
        }
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
