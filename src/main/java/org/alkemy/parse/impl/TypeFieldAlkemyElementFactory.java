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
import java.util.Collections;
import java.util.List;

import org.alkemy.util.AnnotationUtils;
import org.alkemy.util.TypedTable;

public class TypeFieldAlkemyElementFactory implements AlkemyElementFactory<AnnotatedElement>
{
    private TypeFieldAlkemyElementFactory()
    {
    }

    public static AlkemyElementFactory<AnnotatedElement> create()
    {
        return new TypeFieldAlkemyElementFactory();
    }

    @Override
    public AbstractAlkemyElement<?> createLeaf(AnnotatedElement desc, ValueAccessor valueAccessor, TypedTable context)
    {
        return AbstractAlkemyElement.create(desc, AccessorFactory.notSupported(), valueAccessor, Collections.emptyList(),
                AnnotationUtils.findVisitorType(desc), false, context);
    }

    @Override
    public AbstractAlkemyElement<?> createNode(AnnotatedElement desc, NodeConstructor valueConstructor,
            ValueAccessor valueAccessor, List<MethodInvoker> methodInvokers, Class<?> nodeType, TypedTable context)
    {
        return AbstractAlkemyElement.create(desc, valueConstructor, valueAccessor, methodInvokers,
                AnnotationUtils.findVisitorType(desc), true, context);
    }
}
