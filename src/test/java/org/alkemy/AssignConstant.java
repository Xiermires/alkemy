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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.visitor.AlkemyElementVisitor;

public class AssignConstant<T> implements AlkemyElementVisitor<AlkemyElement>
{
    private final T t;

    AssignConstant(T t)
    {
        this.t = t;
    }

    @Override
    public void visit(AlkemyElement e, Object parent)
    {
        e.set(t, parent);
    }

    @Override
    public AlkemyElement map(AlkemyElement e)
    {
        return e;
    }

    @Override
    public boolean accepts(Class<?> type)
    {
        return AssignConstant.class.equals(type);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf(AssignConstant.class)
    public @interface Bar
    {
    }
}
