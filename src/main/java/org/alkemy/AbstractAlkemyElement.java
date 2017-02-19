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
package org.alkemy;

import java.lang.reflect.AnnotatedElement;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.impl.NodeConstructor;
import org.alkemy.util.Assertions;
import org.alkemy.visitor.AlkemyElementVisitor;

public abstract class AbstractAlkemyElement<E extends AbstractAlkemyElement<E>> implements ValueAccessor, NodeConstructor
{
    private final AnnotatedElement desc;
    private final ValueAccessor valueAccessor;
    private final NodeConstructor nodeConstructor;
    private final Class<? extends AlkemyElementVisitor<?>> visitorType;
    private final boolean node;
    private final boolean ordered;

    AbstractAlkemyElement(AnnotatedElement desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
            Class<? extends AlkemyElementVisitor<?>> visitorType, boolean node, boolean ordered)
    {
        this.desc = desc;
        this.valueAccessor = valueAccessor;
        this.nodeConstructor = nodeConstructor;
        this.visitorType = visitorType;
        this.node = node;
        this.ordered = ordered;
    }

    protected AbstractAlkemyElement(AbstractAlkemyElement<?> other)
    {
        Assertions.exists(other);

        this.desc = other.desc;
        this.valueAccessor = other.valueAccessor;
        this.nodeConstructor = other.nodeConstructor;
        this.visitorType = other.visitorType;
        this.node = other.node;
        this.ordered = other.ordered;
    }

    public static AlkemyElement create(AnnotatedElement desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
            Class<? extends AlkemyElementVisitor<?>> visitorType, boolean node, boolean ordered)
    {
        return new AlkemyElement(desc, nodeConstructor, valueAccessor, visitorType, node, ordered);
    }

    public AnnotatedElement desc()
    {
        return desc;
    }

    public Class<? extends AlkemyElementVisitor<?>> visitorType()
    {
        return visitorType;
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return valueAccessor.type();
    }

    @Override
    public <T> T newInstance(Object... args) throws AlkemyException
    {
        if (node) { return nodeConstructor.newInstance(args); }
        throw new AlkemyException("Alkemy elements w/o children cannot be instantiated");
    }

    @Override
    public Object get(Object parent) throws AccessException
    {
        return valueAccessor.get(parent);
    }

    @Override
    public void set(Object value, Object parent) throws AccessException
    {
        valueAccessor.set(value, parent);
    }

    @Override
    public String targetName()
    {
        return valueAccessor.targetName();
    }

    public <T extends AbstractAlkemyElement<T>> Object accept(AlkemyElementVisitor<T> v)
    {
        final T t = map(v);
        return v.visit(t);
    }
    
    public <T extends AbstractAlkemyElement<T>> void accept(AlkemyElementVisitor<T> v, Object parent)
    {
        final T t = map(v);
        v.visit(t, parent);
    }

    public <T extends AbstractAlkemyElement<T>> Object accept(AlkemyElementVisitor<T> v, Object... args)
    {
        final T t = map(v);
        return v.visit(t, args);
    }
    
    public <T extends AbstractAlkemyElement<T>> void accept(AlkemyElementVisitor<T> v, Object parent, Object... args)
    {
        final T t = map(v);
        v.visit(t, parent, args);
    }
    
    private Object cachedRef = null;
    
    @SuppressWarnings("unchecked")
    // safe so long v#map() maps statically { (consider) AlkemyElement e = ... (then) v.map(e) = v.map(e) = v.map(e) = ...
    // extend otherwise.
    protected <T extends AbstractAlkemyElement<T>> T map(AlkemyElementVisitor<T> v)
    {
        return (T) (cachedRef == null ? (cachedRef = v.map(new AlkemyElement(this))) : cachedRef);
    }

    public boolean isOrdered()
    {
        return ordered;
    }

    public boolean isNode()
    {
        return node;
    }

    public static class AlkemyElement extends AbstractAlkemyElement<AlkemyElement>
    {
        Object o;

        public AlkemyElement(AbstractAlkemyElement<?> other)
        {
            super(other);
        }

        AlkemyElement(AnnotatedElement desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
                Class<? extends AlkemyElementVisitor<?>> visitorType, boolean node, boolean ordered)
        {
            super(desc, nodeConstructor, valueAccessor, visitorType, node, ordered);
        }
    }
}
