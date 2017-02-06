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
package org.alkemy.methodhandle;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.alkemy.core.AbstractValueAccessor;
import org.alkemy.exception.AccessException;
import org.alkemy.exception.TargetException;

public class MemberMethodHandleAccessor extends AbstractValueAccessor
{
    private final Class<?> declaringClass;
    private final String name;
    private final Class<?> type;
    private final Function<Object, ?> getter;
    private final BiConsumer<Object, Object> setter;
    
    MemberMethodHandleAccessor(Class<?> clazz, String name, Class<?> type, Function<Object, ?> getter, BiConsumer<Object, Object> setter)
    {
        this.declaringClass = clazz;
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Class<?> getType() throws TargetException
    {
        return type;
    }

    @Override
    public Object get() throws AccessException
    {
        return Objects.nonNull(bound) ? getter.apply(bound) : null;
    }

    @Override
    public void set(Object value) throws AccessException
    {
        if (Objects.nonNull(bound))
        {
            setter.accept(bound, value);
        }
    }

    @Override
    public String getTargetName()
    {
        return name;
    }
    
    @Override
    protected Class<?> getDeclaringClass()
    {
        return declaringClass;
    }
}
