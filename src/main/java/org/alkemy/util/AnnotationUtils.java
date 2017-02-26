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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.exception.AlkemyException;

public class AnnotationUtils
{
    public static Class<? extends Annotation> findAlkemyTypes(AnnotatedElement ae)
    {
        final List<Pair<Annotation, AlkemyLeaf>> alkemyType = AnnotationUtils.getAnnotationsQualifiedAs(ae, AlkemyLeaf.class);
        if (alkemyType.size() > 1)
        {
            throw new AlkemyException("Invalid configuration. Multiple alkemy visitors defined for a single element.");
        }
        return alkemyType.isEmpty() ? null : alkemyType.get(0).second.value();
    }

    private static <QualifyingType extends Annotation> List<Pair<Annotation, QualifyingType>> getAnnotationsQualifiedAs(
            AnnotatedElement target, Class<QualifyingType> type)
    {
        return getAnnotationsQualifiedAs(target.getAnnotations(), type);
    }

    private static <QualifyingType extends Annotation> List<Pair<Annotation, QualifyingType>> getAnnotationsQualifiedAs(
            Annotation[] as, Class<QualifyingType> type)
    {
        final List<Pair<Annotation, QualifyingType>> pairs = new ArrayList<Pair<Annotation, QualifyingType>>();
        for (final Annotation a : as)
        {
            if (a.annotationType().isAnnotationPresent(type))
            {
                pairs.add(Pair.create(a, a.annotationType().getAnnotation(type)));
            }
        }
        return pairs;
    }
}
