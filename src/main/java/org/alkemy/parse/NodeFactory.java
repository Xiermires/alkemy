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
package org.alkemy.parse;

import java.util.Collection;
import java.util.List;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;

import com.google.common.collect.Lists;

public interface NodeFactory extends AutoCastValueAccessor
{
    /**
     * if {@link #isCollection()}, then adds the values to it.
     * 
     * @throws AccessException
     *             If an error occurs while adding the values.
     */
    @SuppressWarnings("unchecked")
    default <E> void addAll(Object parent, E first, E... others) throws AlkemyException {
        addAll(parent, Lists.asList(first, others));
    }
    
    @SuppressWarnings("unchecked")
    default <E> void addAll(Object parent, List<E> others) throws AlkemyException {
        if (isCollection() && !others.isEmpty() && componentType().isInstance(others.get(0)))
        {
            final Collection<E> c = get(parent, Collection.class);
            if (c != null)
            {
                c.addAll(others);
            }
        }
    }
    
    /**
     * Returns a new instance of the class.
     * 
     * @throws AlkemyException
     *             If an error occurs while creating the class instance.
     */
    Object newInstance(Object... args) throws AlkemyException;

    /**
     * Returns a new instance of the component class, or null if not a component type.
     * 
     * @throws AlkemyException
     *             If an error occurs while creating the class instance.
     */
    Object newComponentInstance(Object... args) throws AlkemyException;

    /**
     * Returns a new instance of the component class if is assignable to T, null otherwise.
     * 
     * @throws AlkemyException
     *             If an error occurs while creating the class instance.
     */
    @SuppressWarnings("unchecked")
    // safe
    default <E> E newInstance(Class<E> type, Object... args) throws AlkemyException
    {
        final Object v = newInstance(args);
        return v != null && type.isInstance(v) ? (E) v : null;
    }

    /**
     * Returns a new instance of the component class if is assignable to T, null otherwise.
     * 
     * @throws AlkemyException
     *             If an error occurs while creating the class instance.
     */
    @SuppressWarnings("unchecked")
    // safe
    default <E> E newComponentInstance(Class<E> type, Object... args) throws AlkemyException
    {
        final Object v = newComponentInstance(args);
        return v != null && type.isInstance(v) ? (E) v : null;
    }
}
