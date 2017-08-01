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
package org.alkemy.parse.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;

public class MemberFieldLambdaBasedAccessor implements ValueAccessor
{
    private final String name;
    private final Class<?> type;
    private final Function<Object, ?> getter;
    private final BiConsumer<Object, Object> setter;
    private final boolean collection;

    MemberFieldLambdaBasedAccessor(String name, Class<?> type, Function<Object, ?> getter, BiConsumer<Object, Object> setter)
    {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
        this.collection = Collection.class.isAssignableFrom(type);
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    // safe
    public Class<? extends Collection<Object>> collectionType() throws AlkemyException
    {
        return collection ? (Class<? extends Collection<Object>>) type : null;
    }
    
    @Override
    public Object get(Object parent) throws AccessException
    {
        return Objects.nonNull(parent) ? getter.apply(parent) : null;
    }

    @Override
    public void set(Object value, Object parent) throws AccessException
    {
        if (Objects.nonNull(parent))
        {
            setter.accept(parent, value);
        }
    }

    @Override
    public String targetName()
    {
        return name;
    }

    @Override
    public boolean isCollection()
    {
        return collection;
    }

    @Override
    @SuppressWarnings("unchecked")
    // safe
    public void add(Object value, Object parent) throws AlkemyException
    {
        if (collection)
        {
            final Collection<Object> col = (Collection<Object>) get(parent);
            if (col != null)
            {
                col.add(value);
            }
        }
    }
}
