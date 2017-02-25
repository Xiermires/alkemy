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

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.alkemy.exception.AlkemyException;

public interface MethodInvoker
{
    AnnotatedElement desc();
    
    String name();
    
    Class<?> declaringClass();
    
    /**
     * Invokes the method and returns the value if any (can be void).
     * 
     * @throws AlkemyException
     *             If an error occurs while invoking the method.
     */
    Optional<Object> invoke(Object parent, Object... args) throws AlkemyException;
    
    /**
     * Invokes the method and returns the value if is exactly of type T (not assignable!), null otherwise.
     * 
     * @throws AlkemyException
     *             If an error occurs while invoking the method.
     */
    <R> R safeInvoke(Object parent, Class<R> retType, Object... args) throws AlkemyException;
}
