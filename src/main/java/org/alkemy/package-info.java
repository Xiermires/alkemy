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

/**
 * The Alkemy library allows applying user specific {@link AlkemyNodeReader},
 * {@link AlkemyNodeVisitor} and {@link AlkemyElementVisitor} strategies to a set of alkemy
 * elements.
 * <p>
 * Alkemy elements are the result of parsing an "alkemized" type with an {@link AlkemyParser}, being
 * the concept of "alkemized" and the parser implementation user defined.
 * <p>
 * For instance the {@link AlkemyParsers#fieldParser()} searches fields "super" annotated as
 * {@link AlkemyLeaf} within the type hierarchy and groups them under a directed rooted tree
 * starting from the parsed type. The resulting tree contains all 'alkemizations' of the type.
 * <p>
 * Alkemized types can contain sub-types which also contain alkemizations and so on. Each of this
 * types is represented as a starting branch in the tree.
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
 * <li>{@link org.alkemy.visitor.AlkemyNodeVisitor}
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

