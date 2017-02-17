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

import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Conditions;
import org.alkemy.util.Node;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyTypeVisitor;

public class Alkemist
{
    private AlkemyLoadingCache cache;
    private AlkemyElementVisitor<?> aev;

    Alkemist(AlkemyLoadingCache cache, AlkemyElementVisitor<?> aev)
    {
        Conditions.requireNonNull(cache, aev);

        this.cache = cache;
        this.aev = aev;
    }

    public <T> T process(T t)
    {
        Conditions.requireNonNull(t);

        final Node<? extends AbstractAlkemyElement<?>> root = cache.get(t.getClass());
        root.children().forEach(c -> process(c, t));
        return t;
    }

    // Default top->Down strategy.
    // TODO: AlkemyTypeVisitor defines the traverse strategy.
    private void process(Node<? extends AbstractAlkemyElement<?>> e, Object parent)
    {
        if (e.hasChildren())
        {
            e.children().forEach(c -> process(c, e.data().get(parent)));
        }
        else
        {
            if (aev.getClass().equals(e.data().visitorType()))
            {
                e.data().accept(aev, parent);
            }
        }
    }

    public static <T> T process(T t, AlkemyTypeVisitor atv)
    {
        Conditions.requireNonNull(t, atv);

        atv.visit(AlkemyParsers.fieldParser().parse(t.getClass()));
        return t;
    }
}
