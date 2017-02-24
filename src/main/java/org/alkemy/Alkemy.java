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
package org.alkemy;

import org.agenttools.AgentTools;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AlkemizerCTF;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeReader;
import org.alkemy.visitor.AlkemyNodeVisitor;

/**
 * The Alkemy library allows applying user specific {@link AlkemyNodeReader}, {@link AlkemyNodeVisitor} and
 * {@link AlkemyElementVisitor} strategies to a set of alkemy elements.
 * <p>
 * Alkemy elements are the result of parsing an "alkemized" type with an {@link AlkemyParser}, being "alkemized" and the parser
 * user defined.
 * <p>
 * For instance the {@link AlkemyParsers#fieldParser()} searches fields "super" annotated as {@link AlkemyLeaf} within the type
 * hierarchy and groups them as a directed rooted tree starting from the parsed type.
 * <p>
 * If a type define alkemizations which are not supported by the used visitors, those alkemy elements are left unprocessed.
 */
public class Alkemy
{
    /**
     * This method set-up the instrumentation of classes.
     * <p>
     * It is very important that the method is called before any of the alkemized classes are loaded by the class loader for the
     * instrumentation to work. If instrumentation fails, the library fallbacks to reflection which is considerably slower (~10x).
     * <p>
     * Only alkemized classes are instrumented, others are left untouched.
     */
    public static void startAlkemizer()
    {
        AgentTools.add(new AlkemizerCTF());
    }

    /**
     * As {@link #nodes(AlkemyParser)} but using the {@link AlkemyParsers#fieldParser()} by default.
     */
    public static NodeFactory nodes()
    {
        return new AlkemyNodes(AlkemyParsers.fieldParser());
    }

    /**
     * The node factory is the starting point of any alkemizing process.
     * <p>
     * Types can be converted into directed rooted trees of alkemy elements, which can be later feed to any
     * {@link AlkemyNodeVisitor} and {@link AlkemyNodeReader} implementations.
     * <p>
     * Nodes are created using an {@link AlkemyParser} which is responsible for finding defined alkemizations and creating alkemy
     * elements out of a type.
     */
    public static NodeFactory nodes(AlkemyParser parser)
    {
        return new AlkemyNodes(parser);
    }
    
    public static interface NodeFactory
    {
        <R> TypifiedNode<R, ? extends AbstractAlkemyElement<?>> get(Class<R> type);
    }
}
