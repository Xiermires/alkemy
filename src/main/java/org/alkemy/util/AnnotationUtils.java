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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.exception.AlkemyException;

public class AnnotationUtils
{
    /**
     * If the annotated element is an alkemy element, it returns its type.
     * <p>
     * The alkemy type either, the {@link AlkemyLeaf#value()} if specified, or the annotation
     * qualified as {@link AlkemyLeaf} type.
     */
    public static Class<? extends Annotation> findAlkemyTypes(AnnotatedElement ae)
    {
        final List<Pair<Annotation, AlkemyLeaf>> alkemyType = AnnotationUtils.getAnnotationsQualifiedAs(ae, AlkemyLeaf.class);
        if (alkemyType.size() > 1)
        {
            throw new AlkemyException("Invalid configuration. Multiple alkemy visitors defined for a single element.");
        }
        if (alkemyType.isEmpty())
        {
            return null;
        }
        else
        {
            final Class<? extends Annotation> value = alkemyType.get(0).second.value();
            return Annotation.class == value ? alkemyType.get(0).first.annotationType() : value;
        }
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
