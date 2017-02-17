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
import org.alkemy.util.Conditions;
import org.alkemy.visitor.AlkemyElementVisitor;

public abstract class AbstractAlkemyElement<E extends AbstractAlkemyElement<E>> implements ValueAccessor
{
    private final AnnotatedElement desc;
    private final ValueAccessor valueAccessor;
    private final NodeConstructor valueConstructor;
    private final Class<? extends AlkemyElementVisitor<?>> visitorType;

    AbstractAlkemyElement(AnnotatedElement desc, NodeConstructor valueConstructor, ValueAccessor valueAccessor, Class<? extends AlkemyElementVisitor<?>> visitorType)
    {
        this.desc = desc;
        this.valueAccessor = valueAccessor;
        this.valueConstructor = valueConstructor;
        this.visitorType = visitorType;
    }

    protected AbstractAlkemyElement(AbstractAlkemyElement<?> other)
    {
        Conditions.requireNonNull(other);
        
        this.desc = other.desc;
        this.valueAccessor = other.valueAccessor;
        this.valueConstructor = other.valueConstructor;
        this.visitorType = other.visitorType;
    }

    public static AlkemyElement create(AnnotatedElement desc, NodeConstructor valueConstructor, ValueAccessor valueAccessor, Class<? extends AlkemyElementVisitor<?>> visitorType)
    {
        return new AlkemyElement(desc, valueConstructor, valueAccessor, visitorType);
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

    public <T extends AbstractAlkemyElement<T>> T accept(AlkemyElementVisitor<T> v, Object parent)
    {
        final T t = v.map(new AlkemyElement(this));
        v.visit(t, parent);
        return t;
    }

    public static class AlkemyElement extends AbstractAlkemyElement<AlkemyElement>
    {
        Object o;
        
        public AlkemyElement(AbstractAlkemyElement<?> other)
        {
            super(other);
        }

        AlkemyElement(AnnotatedElement desc, NodeConstructor valueConstructor, ValueAccessor valueAccessor, Class<? extends AlkemyElementVisitor<?>> visitorType)
        {
            super(desc, valueConstructor, valueAccessor, visitorType);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        // safe so long v#map() maps statically { (consider) AlkemyElement e = ... (then) v.map(e) = v.map(e) = v.map(e) = ...
        // TODO: Allow to switch it off. 
        public <T extends AbstractAlkemyElement<T>> T accept(AlkemyElementVisitor<T> v, Object parent)
        {
            return (T) (o == null ? (o = super.accept(v, parent)) : o);
        }
    }
}
