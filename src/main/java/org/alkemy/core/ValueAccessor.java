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

import org.alkemy.exception.AccessException;
import org.alkemy.exception.TargetException;

public interface ValueAccessor extends Bound<Object>
{
    /**
     * Returns the value type.
     * 
     * @throws AccessException
     *             If an error occurs while recovering the value type.
     */
    Class<?> getType() throws TargetException;

    /**
     * Returns the value.
     * 
     * @throws AccessException
     *             If an error occurs while recovering the value.
     */
    Object get() throws AccessException;

    /**
     * Sets a value.
     * 
     * @throws AccessException
     *             If an error occurs while setting the value.
     */
    void set(Object value) throws AccessException;

    /**
     * Returns the target name. Each target name is unique in the owner context.
     */
    String getTargetName();
}
