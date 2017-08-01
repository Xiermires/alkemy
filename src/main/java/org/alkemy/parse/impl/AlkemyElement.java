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
import org.alkemy.util.Assertions;
import org.alkemy.util.TypedTable;

import com.google.common.collect.Table;

public class AlkemyElement implements ValueAccessor, NodeConstructor
{
    private final AnnotatedMember desc;
    private final ValueAccessor valueAccessor;
    private final NodeConstructor nodeConstructor;
    private final Map<String, MethodInvoker> methodInvokers;
    private final Class<? extends Annotation> alkemyType;
    private final boolean node;
    private final TypedTable context;
    private final boolean collection;

    AlkemyElement(AnnotatedMember desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
            List<MethodInvoker> methodInvokers, Class<? extends Annotation> alkemyType, boolean node, TypedTable context)
    {
        this.desc = desc;
        this.valueAccessor = valueAccessor;
        this.nodeConstructor = nodeConstructor;
        this.methodInvokers = new HashMap<String, MethodInvoker>();
        methodInvokers.forEach(c -> this.methodInvokers.put(c.name(), c));
        this.alkemyType = alkemyType;
        this.node = node;
        this.context = context;
        this.collection = Collection.class.isAssignableFrom(desc.getType());
    }

    protected AlkemyElement(AlkemyElement other)
    {
        Assertions.nonNull(other);

        this.desc = other.desc;
        this.valueAccessor = other.valueAccessor;
        this.nodeConstructor = other.nodeConstructor;
        this.methodInvokers = other.methodInvokers;
        this.alkemyType = other.alkemyType;
        this.node = other.node;
        this.context = other.context;
        this.collection = other.collection;
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
        if (node) { return nodeConstructor.newInstance(args); }
        throw new AlkemyException("Alkemy elements w/o children cannot be instantiated");
    }

    @Override
    public <T> T newInstance(Class<T> type, Object... args) throws AlkemyException
    {
        return nodeConstructor.newInstance(type, args);
    }

    @Override
    public Object get(Object parent) throws AccessException
    {
        return valueAccessor.get(parent);
    }

    @Override
    public <T> T get(Object parent, Class<T> type) throws AccessException
    {
        return valueAccessor.get(parent, type);
    }

    @Override
    public void set(Object value, Object parent) throws AccessException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public Class<?> componentType() throws AlkemyException
    {
        return nodeConstructor.componentType();
    }

    @Override
    public Object newComponentInstance(Object... args) throws AlkemyException
    {
        return nodeConstructor.newComponentInstance(args);
    }

    @Override
    public <T> T newComponentInstance(Class<T> type, Object... args) throws AlkemyException
    {
        return nodeConstructor.newComponentInstance(type, args);
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
    public String targetName()
    {
        return valueAccessor.targetName();
    }

    public boolean isNode()
    {
        return node;
    }

    /**
     * A shared context between all nodes in a tree.
     */
    public Table<String, Class<?>, Object> getContext()
    {
        return context;
    }

    @Override
    public String toString()
    {
        return valueAccessor.targetName();
    }

    @Override
    public boolean isCollection()
    {
        return collection;
    }

    @Override
    public void add(Object value, Object parent) throws AlkemyException
    {
        valueAccessor.add(value, parent);
    }

    @Override
    public Class<? extends Collection<Object>> collectionType()
    {
        return valueAccessor.collectionType();
    }
}
