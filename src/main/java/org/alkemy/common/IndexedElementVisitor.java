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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.common.IndexedElementVisitor.IndexedElement;
import org.alkemy.visitor.AlkemyElementVisitor;

public class IndexedElementVisitor implements AlkemyElementVisitor<IndexedElement>
{
    private BiFunction<Integer, Object, Object> f;

    public IndexedElementVisitor(BiFunction<Integer, Object, Object> f)
    {
        this.f = f;
    }

    @Override
    public void visit(IndexedElement e, Object parent)
    {
        final IndexedElement ie = e;
        f.apply(ie.value, ie.get(parent));
    }

    @Override
    public IndexedElement map(AlkemyElement e)
    {
        return new IndexedElement(e);
    }

    @Override
    public boolean accepts(Class<?> type)
    {
        return IndexedElementVisitor.class.equals(type);
    }

    static class IndexedElement extends AbstractAlkemyElement<IndexedElement>
    {
        int value;

        protected IndexedElement(AbstractAlkemyElement<?> ae)
        {
            super(ae);
            value = ae.desc().getAnnotation(Index.class).value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf(IndexedElementVisitor.class)
    public @interface Index
    {
        int value();
    }
}
