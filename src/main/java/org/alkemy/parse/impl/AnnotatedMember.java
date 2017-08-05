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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.alkemy.util.Assertions;

/**
 * An annotated element with type && declaring class information.
 */
public class AnnotatedMember implements AnnotatedElement
{
    private final AnnotatedElement annotatedElement;
    private final String name;
    private final Class<?> type;
    private final Class<?> declaringClass;
    private final Class<?> componentType;

    public AnnotatedMember(String name, AnnotatedElement annotatedElement)
    {
        Assertions.nonNull(annotatedElement);

        this.name = name;
        this.annotatedElement = annotatedElement;
        this.type = annotatedElement.getClass();
        this.declaringClass = annotatedElement.getClass();
        this.componentType = null;
    }
    
    public AnnotatedMember(String name, AnnotatedElement annotatedElement, Class<?> type, Class<?> declaringClass, Class<?> componentType)
    {
        Assertions.nonNull(annotatedElement);

        this.name = name;
        this.annotatedElement = annotatedElement;
        this.type = type;
        this.declaringClass = declaringClass;
        this.componentType = componentType;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return annotatedElement.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations()
    {
        return annotatedElement.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations()
    {
        return annotatedElement.getDeclaredAnnotations();
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getDeclaringClass()
    {
        return declaringClass;
    }

    public Class<?> getType()
    {
        return type;
    }
    
    public Class<?> getComponentType()
    {
        return componentType;
    }
}
