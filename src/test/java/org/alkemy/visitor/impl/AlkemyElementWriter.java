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
package org.alkemy.visitor.impl;

import java.util.function.Supplier;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeReader.FluentAlkemyNodeReader;

public class AlkemyElementWriter<R> implements FluentAlkemyNodeReader<R>
{
    @Override
    public R accept(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> root, Class<R> retType)
    {
        final Parameters params = new Parameters(aev, root.children().size());
        root.children().forEach(c -> processNode(aev, c, params));
        return root.data().safeNewInstance(retType, params.get());
    }

    private void processNode(AlkemyElementVisitor<?> aev, Node<? extends AbstractAlkemyElement<?>> e, Parameters params)
    {
        if (e.hasChildren())
        {
            final Parameters childParams = new Parameters(aev, e.children().size());
            e.children().forEach(c -> processNode(aev, c, childParams));
            if (!childParams.unsupported)
            {
                params.add(e.data().newInstance(childParams.get()));
            }
        }
        else
        {
            params.tryAdd(e);
        }
    }

    static class Parameters implements Supplier<Object[]>
    {
        int c = 0;
        Object[] params;
        boolean unsupported = false;
        AlkemyElementVisitor<?> aev;

        Parameters(AlkemyElementVisitor<?> aev, int size)
        {
            this.aev = aev;
            params = new Object[size];
        }

        void add(Object param)
        {
            params[c++] = param;
        }

        void tryAdd(Node<? extends AbstractAlkemyElement<?>> e)
        {
            if (aev.accepts(e.data().alkemyType()))
            {
                params[c++] = e.data().accept(aev);
            }
            else
            {
                unsupported = true;
            }
        }

        @Override
        public Object[] get()
        {
            return params;
        }
    }
}
