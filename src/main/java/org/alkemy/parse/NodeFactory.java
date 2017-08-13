/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
