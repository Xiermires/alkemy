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
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyNodeVisitor;

public class Alkemist
{
    private AlkemyLoadingCache cache;
    private AlkemyNodeVisitor anv;

    Alkemist(AlkemyLoadingCache cache, AlkemyNodeVisitor anv)
    {
        Conditions.requireContentNotNull(cache, anv);

        this.cache = cache;
        this.anv = anv;
    }

    public <T> T process(T t)
    {
        Conditions.requireNonNull(t);
        anv.visit(cache.get(t.getClass()), Reference.in(t));
        return t;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T process(Class<T> clazz)
    {
        Conditions.requireContentNotNull(clazz, anv);
        final Reference<Object> ref = Reference.inOut();
        anv.visit(cache.get(clazz), ref);
        return (T) ref.get();
    }

    @SuppressWarnings("unchecked")
    public static <T> T process(T t, AlkemyNodeVisitor anv)
    {
        Conditions.requireContentNotNull(t, anv);
        final Reference<Object> ref = Reference.inOut();
        anv.visit(AlkemyParsers.fieldParser().parse(t.getClass()), ref);
        return (T) ref.get();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T process(Class<T> clazz, AlkemyNodeVisitor anv)
    {
        Conditions.requireContentNotNull(clazz, anv);
        final Reference<Object> ref = Reference.inOut();
        anv.visit(AlkemyParsers.fieldParser().parse(clazz), ref);
        return (T) ref.get();
    }
}
