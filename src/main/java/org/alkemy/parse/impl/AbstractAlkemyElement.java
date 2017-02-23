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
import java.lang.reflect.AnnotatedElement;
import java.util.Map;

import org.alkemy.ValueAccessor;
import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.util.Assertions;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeReader;
import org.alkemy.visitor.impl.AlkemyControllerVisitor;
import org.alkemy.visitor.impl.AlkemyPostorderReader;
import org.alkemy.visitor.impl.AlkemyPreorderReader;

public abstract class AbstractAlkemyElement<E extends AbstractAlkemyElement<E>> implements ValueAccessor, NodeConstructor
{
    private final AnnotatedElement desc;
    private final ValueAccessor valueAccessor;
    private final NodeConstructor nodeConstructor;
    private final Class<? extends Annotation> alkemyType;
    private final boolean node;
    private final Map<String, Object> context;

    AbstractAlkemyElement(AnnotatedElement desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
            Class<? extends Annotation> alkemyType, boolean node, Map<String, Object> context)
    {
        this.desc = desc;
        this.valueAccessor = valueAccessor;
        this.nodeConstructor = nodeConstructor;
        this.alkemyType = alkemyType;
        this.node = node;
        this.context = context;
    }

    protected AbstractAlkemyElement(AbstractAlkemyElement<?> other)
    {
        Assertions.nonNull(other);

        this.desc = other.desc;
        this.valueAccessor = other.valueAccessor;
        this.nodeConstructor = other.nodeConstructor;
        this.alkemyType = other.alkemyType;
        this.node = other.node;
        this.context = other.context;
    }

    static AlkemyElement create(AnnotatedElement desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
            Class<? extends Annotation> alkemyType, boolean node, Map<String, Object> context)
    {
        return new AlkemyElement(desc, nodeConstructor, valueAccessor, alkemyType, node, context);
    }

    public AnnotatedElement desc()
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
        if (node)
        {
            return nodeConstructor.newInstance(args);
        }
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

    // Maintain a view of the element for disable cacheAcceptedRef() scenarios.
    private AlkemyElement view;

    private AlkemyElement view()
    {
        return view != null ? view : new AlkemyElement(this);
    }

    /**
     * Use this accept method to work on an element which is not linked to any concrete instance of a class.
     * <p>
     * If the visitor doesn't {@link AlkemyElementVisitor#accepts(Class)} this element, element is not processed and returns null.
     * <p>
     * The accept methods are used to bridge between {@link AlkemyNodeReader}, which traverses a tree of unknown
     * {@link AbstractAlkemyElement} implementations, and an {@link AlkemyElementVisitor} which requires an explicit
     * {@link AbstractAlkemyElement} type. Multi-purpose node visitors, such as the {@link AlkemyPreorderReader}, the
     * {@link AlkemyPostorderReader} and the {@link AlkemyControllerVisitor}, use this method to avoid any casting between
     * {@link AbstractAlkemyElement} types.
     * <p>
     * Whenever a {@link AlkemyNodeReader} handles known {@link AbstractAlkemyElement} implementations, these methods are not
     * required and the node visitor can directly
     * <code>alkemyElementVisitor.visit(new ConcreteAlkemyElement(node<impl>.data())</code>.
     */
    public <T extends AbstractAlkemyElement<T>> Object accept(AlkemyElementVisitor<T> v)
    {
        if (cacheAcceptedRef())
        {
            final T t = map(v);
            return t != null ? v.visit(t) : null;
        }
        else return v.visit(v.map(view()));
    }

    /**
     * As {@link #accept(AlkemyElementVisitor, Object)} but accepting extra parameters.
     * <p>
     * Returns true if the visitor could accept this element.
     */
    public <T extends AbstractAlkemyElement<T>> boolean accept(AlkemyElementVisitor<T> v, Object parent, Object... args)
    {
        if (cacheAcceptedRef())
        {
            final T t = map(v);
            if (t != null)
            {
                v.visit(t, parent, args);
            }
        }
        else if (v.accepts(alkemyType)) v.visit(v.map(view()), parent, args);
        else return false;
        return true;
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
            return (T) (v.accepts(alkemyType) ? (cacheRef = v.map(view())) : null);
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

    public boolean isNode()
    {
        return node;
    }

    /**
     * A shared context between all nodes in a tree.
     */
    public Map<String, Object> getContext()
    {
        return context;
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
                Class<? extends Annotation> alkemyType, boolean node, Map<String, Object> context)
        {
            super(desc, nodeConstructor, valueAccessor, alkemyType, node, context);
        }
    }
}
