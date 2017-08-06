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

import java.util.Collections;
import java.util.List;

import org.alkemy.parse.MethodInvoker;
import org.alkemy.parse.NodeFactory;
import org.alkemy.parse.ValueAccessor;
import org.alkemy.util.AnnotationUtils;

public class AnnotatedAlkemyElementFactory implements AlkemyElementFactory<AnnotatedMember>
{
    private AnnotatedAlkemyElementFactory()
    {
    }

    public static AlkemyElementFactory<AnnotatedMember> create()
    {
        return new AnnotatedAlkemyElementFactory();
    }

    @Override
    public AlkemyElement createLeaf(AnnotatedMember desc, ValueAccessor valueAccessor)
    {
        return new AlkemyElement(desc, AccessorFactory.notSupported(), valueAccessor, Collections.emptyList(), AnnotationUtils
                .findAlkemyTypes(desc), false);
    }

    @Override
    public AlkemyElement createNode(AnnotatedMember desc, NodeFactory valueConstructor, ValueAccessor valueAccessor,
            List<MethodInvoker> methodInvokers, Class<?> nodeType)
    {
        return new AlkemyElement(desc, valueConstructor, valueAccessor, methodInvokers, AnnotationUtils.findAlkemyTypes(desc),
                true);
    }
}
