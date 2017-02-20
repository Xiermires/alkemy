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
        if (cacheAcceptedRef())
        {
            final T t = map(v);
            return t != null ? v.visit(t) : null;
        }
        else return v.visit(v.map(new AlkemyElement(this)));
    }

    public <T extends AbstractAlkemyElement<T>> void accept(AlkemyElementVisitor<T> v, Object parent)
    {
        if (cacheAcceptedRef())
        {
            final T t = map(v);
            if (t != null)
            {
                v.visit(t, parent);
            }
        }
        else v.visit(v.map(new AlkemyElement(this)), parent);
    }

    public <T extends AbstractAlkemyElement<T>> Object accept(AlkemyElementVisitor<T> v, Object... args)
    {
        if (cacheAcceptedRef())
        {
            final T t = map(v);
            return t != null ? v.visit(t, args) : null;
        }
        else return v.visit(v.map(new AlkemyElement(this)), args);
    }

    public <T extends AbstractAlkemyElement<T>> void accept(AlkemyElementVisitor<T> v, Object parent, Object... args)
    {
        if (cacheAcceptedRef())
        {
            final T t = map(v);
            if (t != null)
            {
                v.visit(t, parent, args);
            }
        }
        else v.visit(v.map(new AlkemyElement(this)), parent, args);
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractAlkemyElement<T>> T map(AlkemyElementVisitor<T> v)
    {
        if (cacheRef != null)
        {
            return (T) cacheRef;
        }
        else if (node)
        {
            return v.map(new AlkemyElement(this)); // do not cache nodes. Different AEVs might be visiting this node.
        }
        else
        {
            return (T) (v.accepts(visitorType) ? (cacheRef = v.map(new AlkemyElement(this))) : null);
        }
    }

    private Object cacheRef = null;

    // By default, mapped elements are cached.
    // That is safe so long v#map() maps statically { (consider) AlkemyElement e = ... (then) v.map(e) = v.map(e) = v.map(e) = ...
    // Extend this method to return false otherwise.
    protected boolean cacheAcceptedRef()
    {
        return true;
    }

    public boolean isOrdered()
    {
        return ordered;
    }

    public boolean isNode()
    {
        return node;
    }

    @Override
    public String toString()
    {
        return valueAccessor.targetName();
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
