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
import java.util.List;

import org.alkemy.parse.AlkemyLexer;
import org.alkemy.util.AnnotationUtils;
import org.alkemy.util.TypedTable;

class TypeLexer implements AlkemyLexer<Class<?>, AnnotatedElement>
{
    private AlkemyElementFactory<AnnotatedElement> factory;

    private TypeLexer(AlkemyElementFactory<AnnotatedElement> factory)
    {
        this.factory = factory;
    }

    static AlkemyLexer<Class<?>, AnnotatedElement> create(AlkemyElementFactory<AnnotatedElement> factory)
    {
        return new TypeLexer(factory);
    }

    @Override
    public boolean isNode(Class<?> desc)
    {
        final TypeLeafFinder leafFinder = new TypeLeafFinder();
        leafFinder.parse(desc);
        return leafFinder.get();
    }

    @Override
    public boolean isLeaf(AnnotatedElement desc)
    {
        return AnnotationUtils.findVisitorType(desc) != null;
    }

    @Override
    public AbstractAlkemyElement<?> createLeaf(AnnotatedElement desc, ValueAccessor valueAccessor, TypedTable context)
    {
        return factory.createLeaf(desc, valueAccessor, context);
    }

    @Override
    public AbstractAlkemyElement<?> createNode(Class<?> desc, NodeConstructor valueConstructor,
            ValueAccessor valueAccessor, List<MethodInvoker> methodInvokers, Class<?> nodeType, TypedTable context)
    {
        return factory.createNode(desc, valueConstructor, valueAccessor, methodInvokers, nodeType, context);
    }
}
