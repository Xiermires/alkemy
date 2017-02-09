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

import java.lang.reflect.AnnotatedElement;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.AlkemyNode;
import org.alkemy.core.AlkemyElement;
import org.alkemy.core.AlkemyElementFactory;
import org.alkemy.core.ValueAccessor;
import org.alkemy.parse.AlkemyLexer;
import org.alkemy.util.AnnotationUtils;

public class TypeFieldLexer<E extends AlkemyElement> implements AlkemyLexer<E, AnnotatedElement>
{
    private AlkemyElementFactory<E, AnnotatedElement> factory;

    private TypeFieldLexer(AlkemyElementFactory<E, AnnotatedElement> factory)
    {
        this.factory = factory;
    }
    
    public static <E extends AlkemyElement> AlkemyLexer<E, AnnotatedElement> create(AlkemyElementFactory<E, AnnotatedElement> factory)
    {
        return new TypeFieldLexer<E>(factory);
    }
    
    @Override
    public boolean isNode(AnnotatedElement desc)
    {
        // TODO : Deep search for leaves
        return desc.isAnnotationPresent(AlkemyNode.class);
    }

    @Override
    public boolean isLeaf(AnnotatedElement desc)
    {
        return !AnnotationUtils.getAnnotationsQualifiedAs(desc, AlkemyLeaf.class).isEmpty();
    }

    @Override
    public E createLeaf(AnnotatedElement desc, ValueAccessor valueAccessor)
    {
        return factory.createLeaf(desc, valueAccessor);
    }

    @Override
    public E createNode(AnnotatedElement desc, ValueAccessor valueAccessor, Class<?> nodeType)
    {
        return factory.createNode(desc, valueAccessor, nodeType);
    }

}
