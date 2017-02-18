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
package org.alkemy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyNodeVisitor;
import org.alkemy.visitor.impl.AlkemyValueProvider;

public class ObjectFactoryVisitor implements Supplier<Object>, AlkemyNodeVisitor
{
    private Object o;
    private AlkemyValueProvider ap;

    public ObjectFactoryVisitor(AlkemyValueProvider ap)
    {
        this.ap = ap;
    }

    @Override
    public Object get()
    {
        return o;
    }

    @Override
    public void visit(Node<? extends AbstractAlkemyElement<?>> root, Reference<Object> rootObj)
    {
        o = getValue(root);
    }

    private Object getValue(Node<? extends AbstractAlkemyElement<?>> e)
    {
        if (e.hasChildren())
        {
            final List<Object> values = new ArrayList<Object>();
            for (Node<? extends AbstractAlkemyElement<?>> c : e.children())
            {
                values.add(getValue(c));
            }
            return e.data().newInstance(values.toArray());
        }
        return ap.getValue(ap.createKey(e.data()));
    }
}
