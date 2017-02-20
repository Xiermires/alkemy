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
package org.alkemy.visitor;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AbstractAlkemyElement.AlkemyElement;

public interface AlkemyElementVisitor<E extends AbstractAlkemyElement<E>>
{
    default Object visit(E element)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    default void visit(E element, Object parent)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    default Object visit(E node, Object... args)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    default void visit(E node, Object parent, Object... args)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    E map(AlkemyElement e);
    
    default boolean accepts(Class<?> type)
    {
        return getClass().equals(type);
    }
}
