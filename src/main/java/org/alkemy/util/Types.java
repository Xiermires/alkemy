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
package org.alkemy.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class Types
{
    private Types() {
    }

    /**
     * <ul>
     * <li>If the type is an array. Equivalent to {@link Class#getComponentType()}
     * <li>If the type is a collection, returns the collection's defined generic type.
     * <li>Otherwise returns null.
     * </ul>
     */
    public static Class<?> getComponentType(Field f)
    {
        if (f.getType().isArray())
        {
            return f.getType().getComponentType();
        }
        else if (Collection.class.isAssignableFrom(f.getType()))
        {
            final Type genericType = f.getGenericType();
            if (genericType instanceof ParameterizedType)
            {
                final Type[] genericTypes = ((ParameterizedType) genericType).getActualTypeArguments();
                try
                {
                    return Class.forName(genericTypes[0].getTypeName());
                }
                catch (Exception e)
                {
                    return null;
                }
            }
        }
        return null;
    }
}
