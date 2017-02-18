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

import org.alkemy.AbstractAlkemyElement;

public interface AlkemyValueProvider
{
    // enums are slower.
    static final int DOUBLE = 0;
    static final int FLOAT = 1;
    static final int LONG = 2;
    static final int INTEGER = 3;
    static final int SHORT = 4;
    static final int BYTE = 5;
    static final int CHAR = 6;
    static final int BOOLEAN = 7;
    static final int OBJECT = 8;
    
    public static interface Key
    {
        int type();
    }

    Key createKey(AbstractAlkemyElement<?> e);
    
    Object getValue(Key key);
    
    Double getDouble(Key key);
    
    Float getFloat(Key key);
    
    Long getLong(Key key);
    
    Integer getInteger(Key key);
    
    Short getShort(Key key);
    
    Byte getByte(Key key);
    
    Character getChar(Key key);
    
    Boolean getBoolean(Key key);
    
    Object getObject(Key key);
}
