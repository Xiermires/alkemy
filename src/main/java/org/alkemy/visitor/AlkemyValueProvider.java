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

public interface AlkemyValueProvider<E extends AbstractAlkemyElement<E>, P>
{
    Object getValue(E e, P p);
    
    Double getDouble(E e, P p);
    
    Float getFloat(E e, P p);
    
    Long getLong(E e, P p);
    
    Integer getInteger(E e, P p);
    
    Short getShort(E e, P p);
    
    Byte getByte(E e, P p);
    
    Character getChar(E e, P p);
    
    Boolean getBoolean(E e, P p);
    
    Object getObject(E e, P p);
}
