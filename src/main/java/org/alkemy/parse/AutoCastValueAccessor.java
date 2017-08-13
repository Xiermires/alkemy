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

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;

public interface AutoCastValueAccessor
{
    /**
     * Returns the value type.
     */
    Class<?> type();
    
    /**
     * True if the target type can be assignable to a collection.
     */
    boolean isCollection();
    
    /**
     * <ul>
     * <li>If the type is an array. Equivalent to {@link Class#getComponentType()}
     * <li>If the type is a collection, returns the collection's defined generic type.
     * <li>Otherwise returns null.
     * </ul>
     */
    Class<?> componentType();

    /**
     * Returns the value.
     * 
     * @throws AlkemyException
     *             If an error occurs while recovering the value.
     */
    Object get(Object parent) throws AlkemyException;

    /**
     * Returns the value if is assignable to type T, null otherwise.
     * 
     * @throws AlkemyException
     *             If an error occurs while recovering the value.
     */
    @SuppressWarnings("unchecked")
    // safe
    default <E> E get(Object parent, Class<E> type) throws AlkemyException
    {
        final Object v = get(parent);
        return v != null && type.isInstance(v) ? (E) v : null;
    }

    /**
     * Sets a value.
     * 
     * @throws AccessException
     *             If an error occurs while setting the value.
     */
    void set(Object value, Object parent) throws AlkemyException;

    /**
     * Returns an identifier of the target value.
     */
    String valueName();
}
