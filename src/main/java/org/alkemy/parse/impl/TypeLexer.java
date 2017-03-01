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

import java.util.List;

import org.alkemy.parse.AlkemyLexer;
import org.alkemy.util.AnnotationUtils;
import org.alkemy.util.TypedTable;

class TypeLexer implements AlkemyLexer<AnnotatedMember, AnnotatedMember>
{
    private AlkemyElementFactory<AnnotatedMember> factory;

    private TypeLexer(AlkemyElementFactory<AnnotatedMember> factory)
    {
        this.factory = factory;
    }

    static AlkemyLexer<AnnotatedMember, AnnotatedMember> create(AlkemyElementFactory<AnnotatedMember> factory)
    {
        return new TypeLexer(factory);
    }

    @Override
    public boolean isNode(AnnotatedMember desc)
    {
        final TypeLeafFinder leafFinder = new TypeLeafFinder();
        leafFinder.parse(desc.getType());
        return leafFinder.get();
    }

    @Override
    public boolean isLeaf(AnnotatedMember desc)
    {
        return AnnotationUtils.findAlkemyTypes(desc) != null;
    }

    @Override
    public AbstractAlkemyElement<?> createLeaf(AnnotatedMember desc, ValueAccessor valueAccessor, TypedTable context)
    {
        return factory.createLeaf(desc, valueAccessor, context);
    }

    @Override
    public AbstractAlkemyElement<?> createNode(AnnotatedMember desc, NodeConstructor valueConstructor,
            ValueAccessor valueAccessor, List<MethodInvoker> methodInvokers, Class<?> nodeType, TypedTable context)
    {
        return factory.createNode(desc, valueConstructor, valueAccessor, methodInvokers, nodeType, context);
    }
}
