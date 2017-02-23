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
package org.alkemy.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

/**
 * A table which includes value type information as part of the key / column.
 * <p>
 * Each value is guaranteed to hold the same type as the column.
 */
public class TypedTable implements Table<String, Class<?>, Object>
{
    private final Table<String, Class<?>, Object> table = HashBasedTable.create();
    
    @Override
    public boolean contains(Object rowKey, Object columnKey)
    {
        return table.contains(rowKey, columnKey);
    }

    @Override
    public boolean containsRow(Object rowKey)
    {
        return table.containsRow(rowKey);
    }

    @Override
    public boolean containsColumn(Object columnKey)
    {
        return table.containsColumn(columnKey);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return table.containsValue(value);
    }

    @Override
    public Object get(Object rowKey, Object columnKey)
    {
        throw new UnsupportedOperationException("Not allowed. Use safeGet");
    }
    
    public <T> T safeGet(Object rowKey, Class<T> columnKey)
    {
        return columnKey.cast(table.get(rowKey, columnKey));
                
    }

    @Override
    public boolean isEmpty()
    {
        return table.isEmpty();
    }

    @Override
    public int size()
    {
        return table.size();
    }

    @Override
    public void clear()
    {
        table.clear();
    }

    @Override
    public Object put(String rowKey, Class<?> columnKey, Object value)
    {
        throw new UnsupportedOperationException("Not allowed. Use safePut");
    }
    
    public <T> T safePut(String rowKey, Class<T> columnKey, T value)
    {
        return columnKey.cast(table.put(rowKey, columnKey, value));
    }

    @Override
    public void putAll(Table<? extends String, ? extends Class<?>, ? extends Object> table)
    {
        throw new UnsupportedOperationException("Not allowed. Use safePutAll");
    }
    
    public  void safePutAll(TypedTable typedTable)
    {
        for (Cell<String, Class<?>, Object> cell : typedTable.cellSet())
        {
            table.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }

    @Override
    public Object remove(Object rowKey, Object columnKey)
    {
        return table.remove(rowKey, columnKey);
    }

    /**
     * An immutable version of {@link Table#row()}
     */
    @Override
    public Map<Class<?>, Object> row(String rowKey)
    {
        return ImmutableMap.copyOf(table.row(rowKey));
    }

    /**
     * An immutable version of {@link Table#column()}
     */
    @Override
    public Map<String, Object> column(Class<?> columnKey)
    {
        return ImmutableMap.copyOf(table.column(columnKey));
    }

    /**
     * An immutable version of {@link Table#cellSet()}
     */
    @Override
    public Set<Cell<String, Class<?>, Object>> cellSet()
    {
        return ImmutableSet.copyOf(table.cellSet());
    }

    /**
     * An immutable version of {@link Table#rowKeySet()}
     */
    @Override
    public Set<String> rowKeySet()
    {
        return ImmutableSet.copyOf(table.rowKeySet());
    }

    /**
     * An immutable version of {@link Table#columnKeySet()}
     */
    @Override
    public Set<Class<?>> columnKeySet()
    {
        return ImmutableSet.copyOf(table.columnKeySet());
    }

    /**
     * An immutable version of {@link Table#values()}
     */
    @Override
    public Collection<Object> values()
    {
        return ImmutableSet.copyOf(table.values());
    }

    /**
     * An immutable version of {@link Table#rowMap()}
     */
    @Override
    public Map<String, Map<Class<?>, Object>> rowMap()
    {
        return ImmutableMap.copyOf(table.rowMap());
    }

    /**
     * An immutable version of {@link Table#columnMap()}
     */
    @Override
    public Map<Class<?>, Map<String, Object>> columnMap()
    {
        return ImmutableMap.copyOf(table.columnMap());
    }

}
