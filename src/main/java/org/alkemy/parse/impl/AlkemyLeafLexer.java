/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.alkemy.parse.impl;

import java.util.List;

import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.MethodInvoker;
import org.alkemy.parse.NodeFactory;
import org.alkemy.parse.ValueAccessor;
import org.alkemy.util.AnnotationUtils;

public class AlkemyLeafLexer implements AlkemyLexer<AnnotatedMember>
{
    private AlkemyElementFactory<AnnotatedMember> factory;

    private AlkemyLeafLexer(AlkemyElementFactory<AnnotatedMember> factory)
    {
        this.factory = factory;
    }

    public static AlkemyLexer<AnnotatedMember> create(AlkemyElementFactory<AnnotatedMember> factory)
    {
        return new AlkemyLeafLexer(factory);
    }

    @Override
    public boolean isNode(AnnotatedMember desc)
    {
        final DeepLeafSearch leafFinder = new DeepLeafSearch();
        leafFinder.search(desc.type);
        return leafFinder.get();
    }

    @Override
    public boolean isLeaf(AnnotatedMember desc)
    {
        return AnnotationUtils.findAlkemyTypes(desc) != null;
    }

    @Override
    public AlkemyElement createLeaf(AnnotatedMember desc, ValueAccessor valueAccessor)
    {
        return factory.createLeaf(desc, valueAccessor);
    }

    @Override
    public AlkemyElement createNode(AnnotatedMember desc, NodeFactory valueConstructor,
            ValueAccessor valueAccessor, List<MethodInvoker> methodInvokers, Class<?> nodeType)
    {
        return factory.createNode(desc, valueConstructor, valueAccessor, methodInvokers, nodeType);
    }
}
