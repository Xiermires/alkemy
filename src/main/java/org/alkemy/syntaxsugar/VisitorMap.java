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
package org.alkemy.syntaxsugar;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alkemy.visitor.AlkemyElementVisitor;

public class VisitorMap implements Map<Class<? extends AlkemyElementVisitor>, AlkemyElementVisitor>
{
    private final Map<Class<? extends AlkemyElementVisitor>, AlkemyElementVisitor> m;

    VisitorMap(Map<Class<? extends AlkemyElementVisitor>, AlkemyElementVisitor> m)
    {
        this.m = m;
    }
    
    public static VisitorMap of(AlkemyElementVisitor aev)
    {
        Objects.requireNonNull(aev);
        return new VisitorMap(Collections.singletonMap(aev.getClass(), aev));
    }

    @Override
    public int size()
    {
        return m.size();
    }

    @Override
    public boolean isEmpty()
    {
        return m.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return m.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return m.containsValue(value);
    }

    @Override
    public AlkemyElementVisitor get(Object key)
    {
        return m.get(key);
    }

    @Override
    public AlkemyElementVisitor put(Class<? extends AlkemyElementVisitor> key, AlkemyElementVisitor value)
    {
        return m.put(key, value);
    }

    @Override
    public AlkemyElementVisitor remove(Object key)
    {
        return m.remove(key);
    }

    @Override
    public void putAll(Map<? extends Class<? extends AlkemyElementVisitor>, ? extends AlkemyElementVisitor> m)
    {
        //m.putAll(m); // needs unsafe cast
        for (Entry<? extends Class<? extends AlkemyElementVisitor>, ? extends AlkemyElementVisitor> e : m.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear()
    {
        m.clear();
    }

    @Override
    public Set<Class<? extends AlkemyElementVisitor>> keySet()
    {
        return m.keySet();
    }

    @Override
    public Collection<AlkemyElementVisitor> values()
    {
        return m.values();
    }

    @Override
    public Set<Entry<Class<? extends AlkemyElementVisitor>, AlkemyElementVisitor>> entrySet()
    {
        return m.entrySet();
    }
}
