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
import org.alkemy.util.Assertions;
import org.alkemy.visitor.AlkemyNodeVisitor;

public class Alkemist
{
    private AlkemyLoadingCache cache;
    private AlkemyNodeVisitor anv;

    Alkemist(AlkemyLoadingCache cache, AlkemyNodeVisitor anv)
    {
        Assertions.existAll(cache, anv);

        this.cache = cache;
        this.anv = anv;
    }

    public <T> T process(T t)
    {
        Assertions.exists(t);
        anv.visit(cache.get(t.getClass()), t);
        return t;
    }

    public static <T> T process(T t, AlkemyNodeVisitor anv)
    {
        Assertions.existAll(t, anv);
        anv.visit(AlkemyParsers.fieldParser().parse(t.getClass()), t);
        return t;
    }
    
    public <T> T create(Class<T> clazz)
    {
        Assertions.existAll(clazz, anv);
        return clazz.cast(anv.visit(cache.get(clazz)));
    }
    
    public static <T> T create(Class<T> clazz, AlkemyNodeVisitor anv)
    {
        Assertions.existAll(clazz, anv);
        return clazz.cast(anv.visit(AlkemyParsers.fieldParser().parse(clazz)));
    }
}
