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

/**
 * The Alkemy library allows applying user specific {@link AlkemyNodeReader},
 * {@link AlkemyNodeVisitor} and {@link AlkemyElementVisitor} strategies to a set of alkemy
 * elements.
 * <p>
 * Alkemy elements are the result of parsing an "alkemized" type with an {@link AlkemyParser}, being
 * the concept of "alkemized" and the parser implementation user defined.
 * <p>
 * For instance the {@link AlkemyParsers#typeParser()} searches fields & methods "super" annotated as
 * {@link AlkemyLeaf} within the type hierarchy and groups them under a directed rooted tree
 * starting from the parsed type. The resulting tree contains all 'alkemizations' of the type.
 * <p>
 * Alkemized types must include a no-args constructor of any visibility.
 * <p>
 * Alkemized types can contain sub-types which also contain alkemizations and so on. Each of this
 * types is represented as a starting branch in the tree.
 * <p>
 * The order of the alkemizations within the tree is always deterministic whenever instrumentation is on.
 * <br> It can be specified manually by using  the {@link org.alkemy.annotations.Order} annotation,
 * or, if no annotation is found, follows the declaration order within the java class. 
 * If instrumentation is not on, unless Order is in place, it is not possible to guarantee determinism
 * using reflection.
 * <p>
 * If a type define alkemizations which are not supported by the used visitors, those alkemy
 * elements are left unprocessed.
 * <p>
 * The {@link Alkemy} class provides a collection of syntax sugar methods, referred as simple Alkemy, which
 * allows accessing most common functionality in an static manner.
 * <p>
 * When implementing any of the 'org.alkemy.visitor' interfaces
 * <ul>
 * <li>{@link org.alkemy.visitor.AlkemyNodeReader}
 * <li>{@link org.alkemy.visitor.AlkemyNodeHandler}
 * <li>{@link org.alkemy.visitor.AlkemyElementVisitor} 
 * </ul>
 * There is freedom to choose which methods to implement. That is because it is difficult to predict which
 * functionality the implementing classes will be offering and forcing a common signature that allows all
 * would include several unused parameters in most of them.
 * <p>
 * Regarding performance. 
 * <ol>
 * <li>Reading types to create nodes is expensive. To enhance the operation, a caching system for {@Node} 
 * is included. A new cache is created every time {@link Alkemy#nodes()} is called. User is 
 * responsible of handling the cache to reuse it in successive calls. Keep in mind though that some
 * operations might modify the {@link AbstractAlkemyElement} inside the node to enhance performance, which
 * could make the node unusable for some use cases (see {@link AbstractAlkemyElement#useMappedRefCaching()}.
 * <li>For operations where overhead shall be minimal, consider using:
 * {@link org.alkemy.util.AlkemyUtils#mapFlatNodeLeafs(org.alkemy.visitor.AlkemyElementVisitor, org.alkemy.util.Node, Class, boolean)
 * and any of its associated methods, or provide your own {@link AlkemyNodeVisitor},
 * {@link AlkemyNodeReader} implementations.
 * </ol>
 */
package org.alkemy;

