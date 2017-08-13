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
     * Invokes the method and returns the value if is assignable to type T, null otherwise.
     * 
     * @throws AlkemyException
     *             If an error occurs while invoking the method.
     */
    <R> R invoke(Object parent, Class<R> retType, Object... args) throws AlkemyException;
}
