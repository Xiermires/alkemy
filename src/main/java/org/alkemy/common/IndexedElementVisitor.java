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

import java.util.function.BiFunction;

import org.alkemy.AlkemyElement;
import org.alkemy.common.IndexedElementVisitor.IndexedElement;
import org.alkemy.common.annotations.Index;
import org.alkemy.visitor.impl.MappedAlkemyElementVisitor;

public class IndexedElementVisitor extends MappedAlkemyElementVisitor<IndexedElement>
{
    private BiFunction<Integer, Object, Object> f;
    
    public IndexedElementVisitor(BiFunction<Integer, Object, Object> f)
    {
        this.f = f;
    }
    
    @Override
    protected IndexedElement map(AlkemyElement ae)
    {
        return new IndexedElement(ae);
    }

    @Override
    protected void visitMapped(IndexedElement e, Object parent)
    {   
        f.apply(e.value, e.get(parent));
    }
    
    static class IndexedElement extends AlkemyElement
    {
        int value;
        
        protected IndexedElement(AlkemyElement ae)
        {
            super(ae);
            
            value = ae.desc().getAnnotation(Index.class).value();
        }
    }
}
