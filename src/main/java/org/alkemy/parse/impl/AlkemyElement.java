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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.MethodInvoker;
import org.alkemy.parse.NodeFactory;
import org.alkemy.parse.ValueAccessor;
import org.alkemy.util.Assertions;

public class AlkemyElement implements ValueAccessor, NodeFactory
{
    private final AnnotatedMember desc;
    private final ValueAccessor valueAccessor;
    private final NodeFactory nodeFactory;
    private final Map<String, MethodInvoker> methodInvokers;
    private final Class<? extends Annotation> alkemyType;
    private final boolean node;

    AlkemyElement(AnnotatedMember desc, NodeFactory nodeFactory, ValueAccessor valueAccessor, List<MethodInvoker> methodInvokers,
            Class<? extends Annotation> alkemyType, boolean node)
    {
        this.desc = desc;
        this.valueAccessor = valueAccessor;
        this.nodeFactory = nodeFactory;
        this.methodInvokers = new HashMap<String, MethodInvoker>();
        methodInvokers.forEach(c -> this.methodInvokers.put(c.name(), c));
        this.alkemyType = alkemyType;
        this.node = node;
    }

    protected AlkemyElement(AlkemyElement other)
    {
        Assertions.nonNull(other);

        this.desc = other.desc;
        this.valueAccessor = other.valueAccessor;
        this.nodeFactory = other.nodeFactory;
        this.methodInvokers = other.methodInvokers;
        this.alkemyType = other.alkemyType;
        this.node = other.node;
    }

    public AnnotatedMember desc()
    {
        return desc;
    }

    public Class<? extends Annotation> alkemyType()
    {
        return alkemyType;
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return valueAccessor.type();
    }

    @Override
    public Object newInstance(Object... args) throws AlkemyException
    {
        if (node) { return nodeFactory.newInstance(args); }
        throw new AlkemyException("Alkemy elements w/o children cannot be instantiated");
    }

    @Override
    public <E> E newInstance(Class<E> type, Object... args) throws AlkemyException
    {
        return nodeFactory.newInstance(type, args);
    }

    @Override
    public Object get(Object parent) throws AccessException
    {
        return valueAccessor.get(parent);
    }

    @Override
    public <E> E get(Object parent, Class<E> type) throws AccessException
    {
        return valueAccessor.get(parent, type);
    }

    @Override
    public void set(Object value, Object parent) throws AccessException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public void set(String value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public Class<?> componentType()
    {
        return nodeFactory.componentType();
    }

    @Override
    public Object newComponentInstance(Object... args) throws AlkemyException
    {
        return nodeFactory.newComponentInstance(args);
    }

    @Override
    public <E> E newComponentInstance(Class<E> type, Object... args) throws AlkemyException
    {
        return nodeFactory.newComponentInstance(type, args);
    }

    public Collection<MethodInvoker> getMethodInvokers()
    {
        return methodInvokers.values();
    }

    public MethodInvoker getMethodInvoker(String name)
    {
        return methodInvokers.get(name);
    }

    @Override
    public String valueName()
    {
        return valueAccessor.valueName();
    }

    public boolean isNode()
    {
        return node;
    }

    @Override
    public String toString()
    {
        return valueAccessor.valueName();
    }

    @Override
    public boolean isCollection()
    {
        return nodeFactory.isCollection();
    }

    @Override
    @SafeVarargs
    public final <E> void addAll(Object parent, E first, E... others) throws AlkemyException
    {
        nodeFactory.addAll(parent, first, others);
    }

    @Override
    public final <E> void addAll(Object parent, List<E> others) throws AlkemyException
    {
        nodeFactory.addAll(parent, others);
    }

    @Override
    public void set(double value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public double getDouble(Object parent) throws AlkemyException
    {
        return valueAccessor.getDouble(parent);
    }

    @Override
    public void set(float value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public float getFloat(Object parent) throws AlkemyException
    {
        return valueAccessor.getFloat(parent);
    }

    @Override
    public void set(long value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public long getLong(Object parent) throws AlkemyException
    {
        return valueAccessor.getLong(parent);
    }

    @Override
    public void set(int value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public int getInt(Object parent) throws AlkemyException
    {
        return valueAccessor.getInt(parent);
    }

    @Override
    public void set(short value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public short getShort(Object parent) throws AlkemyException
    {
        return valueAccessor.getShort(parent);
    }

    @Override
    public void set(char value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public char getChar(Object parent) throws AlkemyException
    {
        return valueAccessor.getChar(parent);
    }

    @Override
    public void set(byte value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public byte getByte(Object parent) throws AlkemyException
    {
        return valueAccessor.getByte(parent);
    }

    @Override
    public void set(boolean value, Object parent) throws AlkemyException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public boolean getBoolean(Object parent) throws AlkemyException
    {
        return valueAccessor.getBoolean(parent);
    }
}
