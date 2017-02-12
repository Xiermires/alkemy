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

import java.util.function.Supplier;

import org.alkemy.core.AlkemyElement;
import org.alkemy.core.Bound;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class ObjectCopier<T> implements AlkemyElementVisitor, Bound<Object>, Supplier<T>
{
    private T t;
    private Object origRef;
    private Object destRef;
    private Objenesis objenesis = new ObjenesisStd();

    ObjectCopier(T t)
    {
        this.destRef = this.t = t;
    }

    @Override
    public T get()
    {
        return t;
    }

    @Override
    public void bind(Object t)
    {
        origRef = t;
    }

    @Override
    public Object bound()
    {
        return origRef;
    }

    @Override
    public void visit(Node<? extends AlkemyElement> e)
    {
        visit(e, origRef, destRef);
    }

    private void visit(Node<? extends AlkemyElement> e, Object origRef, Object destRef)
    {
        e.children().forEach(n ->
        {
            n.data().bind(origRef);
            final Object orig = n.data().get();
            if (n.hasChildren())
            {
                n.data().bind(destRef);
                final Object dest = objenesis.newInstance(n.data().type());
                n.data().set(dest);
                visit(n, orig, dest);
            }
            else
            {
                n.data().bind(destRef);
                n.data().set(orig);
            }
        });
    }
}
