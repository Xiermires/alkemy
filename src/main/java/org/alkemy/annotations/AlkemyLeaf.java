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
package org.alkemy.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AbstractAlkemyElement.AlkemyElement;

/**
 * A marker which indicates the annotated annotation annotates itself an alkemy element.
 * 
 * <pre>
 * <code> 
 * 
    <br>@Retention(RetentionPolicy.RUNTIME)
    <br>@Target(ElementType.ANNOTATION_TYPE)
    <br>@AlkemyLeaf(...)
    <br>@interface Marker
    {...}
    <br>
    <br>public class Foo
    <br>{
    <br>    @Marker
    <br>    int foo;
        
    <br>    @Marker
    <br>    int bar;
    <br>}
    <br><br>// creates a directed rooted tree as follows : root (Foo.class) -> { Foo.foo, Foo.bar }
    <br>AlkemyParsers.fieldParser().parse(Foo.class); 
  
 * </code>
 * 
 * Only one annotation annotated as {@link AlkemyElement} is allowed per element. That identifies
 * the type of the alkemization {@link AbstractAlkemyElement#alkemyType()}. This type can be used
 * by the visitors to filter out alkemy types. There is no restriction on how many visitors can
 * support alkemy types, or how many alkemy types can be supported by a visitor.
 * <p>
 * Only one marker per element, doesn't mean all information must be part of the marker. 
 * For instance this is perfectly acceptable:
 * <pre>
 * <code>
 *  <br>public class Foo
    <br>{
    <br>    @Marker
    <br>    int foo;

    <br>    @ReadOnly    
    <br>    @Marker
    <br>    int bar;
    <br>}
 * </code>
 * The visitors can access the extra information via {@link AbstractAlkemyElement#desc()} which
 * returns an {@link AnnotatedElement}, representing a field / method, that can itself be queried 
 * for additional non-marker annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface AlkemyLeaf
{
    Class<? extends Annotation> value(); 
}
