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
package org.alkemy.core;

import java.util.Objects;

import org.alkemy.exception.InvalidBound;

public abstract class AbstractValueAccessor implements ValueAccessor
{
    protected Object bound;

    @Override
    public void bindTo(Object t)
    {
        if (Objects.nonNull(t))
        {
            if (t.getClass().equals(getDeclaringClass()))
            {
                bound = t;
            }
            else
            {
                throw new InvalidBound("Trying to bind class type '%s' with accessor class type '%s'", t.getClass(), getBoundClass());
            }
        }
    }

    protected abstract Class<?> getDeclaringClass();
    
    private Class<?> getBoundClass()
    {
        return Objects.nonNull(bound) ? bound.getClass() : null;
    }
}
