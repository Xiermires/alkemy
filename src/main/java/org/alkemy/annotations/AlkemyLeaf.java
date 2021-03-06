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
package org.alkemy.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

import org.alkemy.parse.impl.AlkemyElement;

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
 * the type of the alkemization {@link AlkemyElement#alkemyType()}. This type can be used
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
 * The visitors can access the extra information via {@link AlkemyElement#desc()} which
 * returns an {@link AnnotatedElement}, representing a field / method, that can itself be queried 
 * for additional non-marker annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface AlkemyLeaf
{
    Class<? extends Annotation> value() default Annotation.class; 
}
