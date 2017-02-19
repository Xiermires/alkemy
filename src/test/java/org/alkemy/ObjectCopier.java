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

import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyNodeVisitor;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class ObjectCopier<T> implements AlkemyNodeVisitor
{
    private Objenesis objenesis = new ObjenesisStd();
    
    @Override
    public Object visit(Node<? extends AbstractAlkemyElement<?>> root, Object orig)
    {
        final Object dest = objenesis.newInstance(orig.getClass());
        visit(root, orig, dest);
        return dest;
    }

    private Object visit(Node<? extends AbstractAlkemyElement<?>> e, Object orig, Object dest)
    {
        e.children().forEach(n ->
        {
            final Object vo = n.data().get(orig);
            if (n.hasChildren())
            {
                final Object vd = objenesis.newInstance(n.data().type());
                n.data().set(vd, dest);
                visit(n, vo, vd);
            }
            else
            {
                n.data().set(vo, dest);
            }
        });
        return dest;
    }
}
