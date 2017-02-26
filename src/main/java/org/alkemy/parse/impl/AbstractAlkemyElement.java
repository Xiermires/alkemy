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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alkemy.Alkemy.NodeFactory;
import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.util.Assertions;
import org.alkemy.util.Node;
import org.alkemy.util.TypedTable;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeReader;
import org.alkemy.visitor.impl.AlkemyPostorderReader;
import org.alkemy.visitor.impl.AlkemyPreorderReader;

import com.google.common.collect.Table;

public abstract class AbstractAlkemyElement<E extends AbstractAlkemyElement<E>> implements ValueAccessor, NodeConstructor
{
    private final AnnotatedElement desc;
    private final ValueAccessor valueAccessor;
    private final NodeConstructor nodeConstructor;
    private final Map<String, MethodInvoker> methodInvokers;
    private final Class<? extends Annotation> alkemyType;
    private final boolean node;
    private final TypedTable context;

    AbstractAlkemyElement(AnnotatedElement desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
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
    }

    protected AbstractAlkemyElement(AbstractAlkemyElement<?> other)
    {
        Assertions.nonNull(other);

        this.desc = other.desc;
        this.valueAccessor = other.valueAccessor;
        this.nodeConstructor = other.nodeConstructor;
        this.methodInvokers = other.methodInvokers;
        this.alkemyType = other.alkemyType;
        this.node = other.node;
        this.context = other.context;
    }

    static AlkemyElement create(AnnotatedElement desc, NodeConstructor nodeConstructor, ValueAccessor valueAccessor,
            List<MethodInvoker> methodInvokers, Class<? extends Annotation> alkemyType, boolean node, TypedTable context)
    {
        return new AlkemyElement(desc, nodeConstructor, valueAccessor, methodInvokers, alkemyType, node, context);
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
    public <T> T safeNewInstance(Class<T> type, Object... args) throws AlkemyException
    {
        return nodeConstructor.safeNewInstance(type, args);
    }

    @Override
    public Object get(Object parent) throws AccessException
    {
        return valueAccessor.get(parent);
    }

    @Override
    public <T> T safeGet(Object parent, Class<T> type) throws AccessException
    {
        return valueAccessor.safeGet(parent, type);
    }

    @Override
    public <T> T getIfAssignable(Object parent, Class<T> type) throws AlkemyException
    {
        return valueAccessor.getIfAssignable(parent, type);
    }

    @Override
    public void set(Object value, Object parent) throws AccessException
    {
        valueAccessor.set(value, parent);
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

    // Maintain a view of the element for disable cacheAcceptedRef() scenarios.
    private AlkemyElement view;

    private AlkemyElement view()
    {
        return view != null ? view : initializeView();
    }

    private synchronized AlkemyElement initializeView()
    {
        if (view == null)
        {
            view = new AlkemyElement(this);
        }
        return view;
    }

    /**
     * Use this accept method to work on an element which is not linked to any concrete instance of
     * a class and return a value associated to it.
     * <p>
     * If the visitor doesn't {@link AlkemyElementVisitor#accepts(Class)} this element, element is
     * not processed and returns null.
     * <p>
     * From the returned value, it is impossible to know whenever a null value is due to the
     * {@link AlkemyElementVisitor#accepts(Class)} returning false, or the visitor itself returning
     * null. If that information is required, the caller itself should invoke the
     * {@link AlkemyElementVisitor#accepts(Class)}.
     * <p>
     * The accept methods are used to bridge between {@link AlkemyNodeReader}, which traverses a
     * tree of unknown {@link AbstractAlkemyElement} implementations, and an
     * {@link AlkemyElementVisitor} which requires an explicit {@link AbstractAlkemyElement} type.
     * Multi-purpose node visitors, such as the {@link AlkemyPreorderReader} and the
     * {@link AlkemyPostorderReader}, use this method to avoid any casting between
     * {@link AbstractAlkemyElement} types.
     * <p>
     * Whenever a {@link AlkemyNodeReader} handles known {@link AbstractAlkemyElement}
     * implementations, these methods are not required and the node visitor can directly
     * <code>alkemyElementVisitor.visit(new ConcreteAlkemyElement(node<impl>.data())</code>.
     */
    public <P, T extends AbstractAlkemyElement<T>> Object accept(AlkemyElementVisitor<P, T> v)
    {
        if (useMappedRefCaching())
        {
            final T t = map(v);
            return t != null ? v.generate(t) : null;
        }
        else if (v.accepts(alkemyType)) return v.generate(v.map(view()));
        else return null;
    }

    /**
     * As {@link #accept(AlkemyElementVisitor, Object)} but accepting extra parameters.
     * <p>
     * Returns true if the visitor could accept this element.
     */
    public <P, T extends AbstractAlkemyElement<T>> boolean accept(AlkemyElementVisitor<P, T> v, Object parent)
    {
        if (useMappedRefCaching())
        {
            final T t = map(v);
            if (t != null)
            {
                v.visit(t, parent);
            }
        }
        else if (v.accepts(alkemyType)) v.visit(v.map(view()), parent);
        else return false;
        return true;
    }

    /**
     * As {@link #accept(AlkemyElementVisitor, Object)} but accepting extra parameters.
     * <p>
     * Returns true if the visitor could accept this element.
     */
    public <P, T extends AbstractAlkemyElement<T>> boolean accept(AlkemyElementVisitor<P, T> v, Object parent, P parameter)
    {
        if (useMappedRefCaching())
        {
            final T t = map(v);
            if (t != null)
            {
                v.visit(t, parent, parameter);
            }
        }
        else if (v.accepts(alkemyType)) v.visit(v.map(view()), parent, parameter);
        else return false;
        return true;
    }

    /**
     * See {@link #useMappedRefCaching()}
     */
    @SuppressWarnings("unchecked")
    protected <P, T extends AbstractAlkemyElement<T>> T map(AlkemyElementVisitor<P, T> v)
    {
        if (cacheRef != null)
        {
            return (T) cacheRef;
        }
        else if (node)
        {
            return v.map(new AlkemyElement(this)); // do not cache nodes. Different AEVs might be
                                                   // visiting this node.
        }
        else
        {
            return (T) (v.accepts(alkemyType) ? initializeCacheRef(v) : null);
        }
    }

    private synchronized <P, T extends AbstractAlkemyElement<T>> Object initializeCacheRef(AlkemyElementVisitor<P, T> v)
    {
        if (cacheRef == null)
        {
            cacheRef = v.map(view());
        }
        return cacheRef;
    }

    private Object cacheRef = null;

    /**
     * By default, mapped elements are cached to enhance the visiting performance.
     * <p>
     * That is safe so long:
     * <ul>
     * <li>v#map() maps statically for all visitors accepting this element <br>
     * { (consider) AlkemyElement e = ... (then) v.map(e) = v.map(e) = v.map(e) = ...
     * <li>Further visitors accepting this element are consistent with the mapped type (either using
     * it directly, or use a super class from it).
     * <li>Visitors work on isolated trees generated by the {@link NodeFactory} or copied from a raw
     * node (not visited) using
     * {@link Node#copy(Node, org.alkemy.util.Node.Builder, java.util.function.Function)}.
     * </ul>
     * <p>
     * Extend this method to return false otherwise.
     */
    protected boolean useMappedRefCaching()
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
    public Table<String, Class<?>, Object> getContext()
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
                List<MethodInvoker> methodInvokers, Class<? extends Annotation> alkemyType, boolean node, TypedTable context)
        {
            super(desc, nodeConstructor, valueAccessor, methodInvokers, alkemyType, node, context);
        }
    }
}
