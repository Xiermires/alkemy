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
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.util.AnnotationUtils;
import org.alkemy.util.Node;
import org.alkemy.util.TypedTable;

public class TypeLeafFinder implements AlkemyParser, AlkemyLexer<Class<?>, AnnotatedElement>, Supplier<Boolean>
{
    private boolean leafFound;
    
    @Override
    public Node<AlkemyElement> parse(Class<?> type)
    {
        for (final Field f : type.getDeclaredFields())
        {
            if (isLeaf(f))
            {
                leafFound = true;
                break;
            }
            else if (isNode(f.getType()))
            {
                // nothing
            }
        }
        return null;
    }
    
    @Override
    public boolean isNode(Class<?> desc)
    {
        final TypeLeafFinder leafFinder = new TypeLeafFinder();
        leafFinder.parse(desc);
        leafFound = leafFinder.get();
        return false;
    }

    @Override
    public boolean isLeaf(AnnotatedElement desc)
    {
        return AnnotationUtils.findAlkemyTypes(desc) != null;
    }

    @Override
    public AlkemyElement createLeaf(AnnotatedElement desc, ValueAccessor valueAccessor, TypedTable context)
    {
        return null;
    }

    @Override
    public AlkemyElement createNode(Class<?> desc, NodeConstructor valueConstructor, ValueAccessor valueAccessor,
            List<MethodInvoker> methodInvokers, Class<?> nodeType, TypedTable context)
    {
        return null;
    }

    @Override
    public Boolean get()
    {
        return leafFound;
    }
}
