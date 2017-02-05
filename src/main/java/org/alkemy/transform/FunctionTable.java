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
package org.alkemy.transform;

import java.util.function.Function;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FunctionTable
{
    private final Table<Class<?>, Class<?>, Function<?, ?>> table = HashBasedTable.create();
    
    @SuppressWarnings("unchecked")
    public <R, C> Function<R, C> get(Class<?> r, Class<?> c)
    {
        return (Function<R, C>) table.get(r, c);
    }
    
    public <R, C> Function<R, C> put(Class<R> r, Class<C> c, Function<R, C> f)
    {
        final Function<R, C> overwritten = get(r, c);
        table.put(r, c, f);
        return overwritten;
    }
    
    public boolean contains(Class<?> r, Class<?> c)
    {
        return table.contains(r, c);
    }
}
