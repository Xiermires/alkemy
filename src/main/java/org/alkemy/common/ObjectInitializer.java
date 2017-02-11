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

import org.alkemy.core.AlkemyElement;
import org.alkemy.core.Bound;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.objenesis.ObjenesisBase;
import org.objenesis.strategy.SerializingInstantiatorStrategy;

public abstract class ObjectInitializer implements AlkemyElementVisitor, Bound<Object>
{
    protected Object o;
    private ObjenesisBase objenesis = new ObjenesisBase(new SerializingInstantiatorStrategy()); // TODO: Instrument Ctor.

    @Override
    public void visit(Node<? extends AlkemyElement> e)
    {
        if (e.hasChildren() && e.data().get() == null)
        {
            e.data().set(objenesis.newInstance(e.data().type()));
        }
        e.data().bindTo(o);
        visit(e.data());
    }

    protected abstract void visit(AlkemyElement e);

    @Override
    public void bindTo(Object o)
    {
        this.o = o;
    }

}
