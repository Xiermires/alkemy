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

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;

public interface ValueAccessor
{
    /**
     * Returns the value type.
     */
    Class<?> type();

    /**
     * Returns the value.
     * 
     * @throws AlkemyException
     *             If an error occurs while recovering the value.
     */
    Object get(Object parent) throws AlkemyException;

    /**
     * Returns the value if is exactly of type T (not assignable!), null otherwise.
     * 
     * @throws AlkemyException
     *             If an error occurs while recovering the value.
     */
    <T> T safeGet(Object parent, Class<T> type) throws AlkemyException;

    /**
     * Returns the value if is a assignable to type T, null otherwise.
     * <p>
     * This method performs worse than {@link #safeGet(Object, Class)}.
     * 
     * @throws AlkemyException
     *             If an error occurs while recovering the value.
     */
    <T> T getIfAssignable(Object parent, Class<T> type) throws AlkemyException;

    /**
     * Sets a value.
     * 
     * @throws AccessException
     *             If an error occurs while setting the value.
     */
    void set(Object value, Object parent) throws AlkemyException;

    String targetName();
}
