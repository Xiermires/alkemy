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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alkemy.visitor.AlkemyElementVisitor;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface AlkemyLeaf
{
    /*
     * Why using a rawtype instead of using something like {@code Class<? extends AlkemyElementVisitor<?>>} ?
     * <p>
     * Using a rawtype allows defining generic parameters in implementations of the AlkemyElementVisitor.
     * <p>
     * Say for instance that we have a class such as follows:
     * <p>
     * 
     * <pre>
     * {@code
     * class Foo<T> implements AlkemyElementVisitor<SomeAlkemyElement>, Supplier<T>
     * <br>{
     * <br> // alkemy stuff 
     * <br> public AlkemyElement map(SomeAlkemyElement e) { ... }
     * <br> public void visit(SomeAlkemyElement e, Object parent) { ... }
     * <br>
     * <br> // some other stuff unrelated to alkemy
     * <br> public T get() { ... }
     * <br>
     * </pre>
     * 
     * If we try assigning this class to an {@link AlkemyLeaf} ({@code @AlkemyLeaf(Foo.class)}). <br>
     * The code doesn't compile due to the generic parameter T in the Foo class.
     * <p>
     * We could workaround this issue by separating the non generic parts of Foo into another class, like:
     * 
     * <pre>
     * {@code
     * class FooBase implements AlkemyElementVisitor<SomeAlkemyElement>
     * <br>{
     * <br> // alkemy stuff 
     * <br> public AlkemyElement map(SomeAlkemyElement e) { ... }
     * <br> public void visit(SomeAlkemyElement e, Object parent) { ... }
     * <br>}
     * <br>class Foo<T> extends FooBase implements Supplier<T>
     * <br>{
     * <br> // some other stuff unrelated to alkemy
     * <br> public T get() { ... }
     * <br>
     * </pre>
     * 
     * Finally assigning FooBase to the AlkemyLeaf ({@code @AlkemyLeaf(FooBase.class)}).
     * <p>
     * This indeed compiles but it :
     * <ol>
     * <li>Adds unnecessary boiler plate code.
     * <li>Forces to some questionable code practices.
     * </ol>
     * Moreover, restricting generic parameters doesn't look right on the first place.
     * <p>
     * Allowing raw classes here, bypasses any parameter restriction, hence allowing ({@code @AlkemyLeaf(Foo.class)}.
     * <p>
     * Is this approach safe ?
     * <p>
     * As safe as defining {@code Class<? extends AlkemyElementVisitor<?>>}. The {@link AlkemyElementVisitor} interface <br>
     * defines the typing restrictions, which we will inherit here as well.
     * <p>
     * Using a rawtype here, leads to an unchecked cast when retrieving the value of the class, see {@link AnnotationUtils#findVisitorType}.
     */
    @SuppressWarnings("rawtypes")
    Class<? extends AlkemyElementVisitor> value();
}
