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

import static org.alkemy.visitor.impl.AbstractTraverser.INSTANTIATE_NODES;

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
import org.alkemy.visitor.impl.AlkemyPostorderReader;
import org.alkemy.visitor.impl.AlkemyPreorderReader;
import org.alkemy.visitor.impl.AlkemyPreorderReader.FluentAlkemyPreorderReader;
import org.alkemy.visitor.impl.SingleTypeReader;
import org.alkemy.visitor.impl.SingleTypeReader.FluentSingleTypeReader;

/**
 * The Alkemy library allows applying user specific {@link AlkemyNodeReader},
 * {@link AlkemyNodeVisitor} and {@link AlkemyElementVisitor} strategies to a set of alkemy
 * elements.
 * <p>
 * Alkemy elements are the result of parsing an "alkemized" type with an {@link AlkemyParser}, being
 * "alkemized" and the parser user defined.
 * <p>
 * For instance the {@link AlkemyParsers#typeParser()} searches fields "super" annotated as
 * {@link AlkemyLeaf} within the type hierarchy and groups them as a directed rooted tree starting
 * from the parsed type.
 * <p>
 * If a type define alkemizations which are not supported by the used visitors, those alkemy
 * elements are left unprocessed.
 */
public class Alkemy
{
    private static boolean instrumenting = false;

    /**
     * This method set-up the instrumentation of classes.
     * <p>
     * It is very important that the method is called before any of the alkemized classes are loaded
     * by the class loader for the instrumentation to work. If instrumentation fails, the library
     * fallbacks to reflection which is considerably slower (~10x).
     * <p>
     * Only alkemized classes are instrumented, others are left untouched.
     */
    public static void startAlkemizer()
    {
        if (!instrumenting)
        {
            AgentTools.add(new AlkemizerCTF());
            instrumenting = true;
        }
    }

    /**
     * As {@link #nodes(AlkemyParser)} but using the {@link AlkemyParsers#typeParser()} by default.
     */
    public static NodeFactory nodes()
    {
        return new AlkemyNodes(AlkemyParsers.typeParser());
    }

    /**
     * Simple alkemy method to parse, create an object and apply the provided visitor to all its
     * leaves.
     * <p>
     * Simple alkemy methods are provided as a quick access to some common, non-intensive uses. But
     * should be avoided in favor of creating specialized facades for the {@link AlkemyNodeVisitor}s
     * and {@link AlkemyNodeReader}s.
     * <ol>
     * <li>Parses the type Class&lt;R&gt; into a Node.
     * <li>Creates an instance of type R.
     * <li>Traverses in pre-order.
     * <li>If any node found is null, it creates and assigns an instance of it.
     * <li>For each leaf calls {@link AlkemyElementVisitor#visit(AbstractAlkemyElement, Object)} on
     * the provided aev.
     * <li>Returns the object R.
     * </ol>
     */
    public static <R, P> R mature(Class<R> r, AlkemyElementVisitor<P, ?> aev)
    {
        return new AlkemyPreorderReader<R, P>(INSTANTIATE_NODES).accept(aev, nodes().get(r));
    }

    /**
     * As {@link #mature(Class, AlkemyElementVisitor)} but including a parameter.
     */
    public static <R, P> R mature(Class<R> r, AlkemyElementVisitor<P, ?> aev, P p)
    {
        return new AlkemyPreorderReader<R, P>(INSTANTIATE_NODES).accept(aev, nodes().get(r), p);
    }

    /**
     * Simple alkemy method to parse an object type and apply the provided visitor to all leaves in
     * non-null branches.
     * <p>
     * Simple alkemy methods are provided as a quick access to some common, non-intensive uses. But
     * should be avoided in favor of creating specialized facades for the {@link AlkemyNodeVisitor}s
     * and {@link AlkemyNodeReader}s.
     * <ol>
     * <li>Parses the type Class&lt;R&gt; into a Node.
     * <li>Traverses in pre-order.
     * <li>If any node found is null, any sub-nodes further in the branch are ignored.
     * <li>For each leaf in a non-null branch calls
     * {@link AlkemyElementVisitor#visit(AbstractAlkemyElement, Object)} on the provided aev.
     * <li>Returns the object R.
     * </ol>
     */
    @SuppressWarnings("unchecked")
    // safe
    public static <R> R mature(R r, AlkemyElementVisitor<R, ?> aev)
    {
        return new FluentAlkemyPreorderReader<R>(0).accept(aev, nodes().get((Class<R>) r.getClass()), r);
    }

    /**
     * As {@link #mature(Object, AlkemyElementVisitor)} but including a parameter.
     */
    @SuppressWarnings("unchecked")
    // safe
    public static <R, P> R mature(R r, P p, AlkemyElementVisitor<P, ?> aev)
    {
        return new AlkemyPreorderReader<R, P>(0).accept(aev, nodes().get((Class<R>) r.getClass()), r, p);
    }

    public static <R> FluentReaderFactory<R> reader(Class<R> retType)
    {
        return new FluentReaderFactory<R>(nodes().get(retType));
    }

    public static <R, P> ReaderFactory<R, P> reader(Class<R> retType, Class<P> paramType)
    {
        return new ReaderFactory<R, P>(nodes().get(retType));
    }

    /**
     * The node factory is the starting point of any alkemizing process.
     * <p>
     * Types can be converted into directed rooted trees of alkemy elements, which can be later feed
     * to any {@link AlkemyNodeVisitor} and {@link AlkemyNodeReader} implementations.
     * <p>
     * Nodes are created using an {@link AlkemyParser} which is responsible for finding defined
     * alkemizations and creating alkemy elements out of a type.
     */
    public static NodeFactory nodes(AlkemyParser parser)
    {
        return new AlkemyNodes(parser);
    }

    public static interface NodeFactory
    {
        <R> TypifiedNode<R, ? extends AbstractAlkemyElement<?>> get(Class<R> type);
    }

    public static class FluentReaderFactory<R>
    {
        private final TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root;

        private FluentReaderFactory(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root)
        {
            this.root = root;
        }

        public FluentSingleTypeReader<R> preorder(int conf)
        {
            return new FluentSingleTypeReader<R>(root, new FluentAlkemyPreorderReader<R>(conf));
        }

        public FluentSingleTypeReader<R> postorder(int conf)
        {
            return new FluentSingleTypeReader<R>(root, new FluentAlkemyPreorderReader<R>(conf));
        }
    }

    public static class ReaderFactory<R, P>
    {
        private final TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root;

        private ReaderFactory(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root)
        {
            this.root = root;
        }

        public SingleTypeReader<R, P> preorder(int conf)
        {
            return new SingleTypeReader<R, P>(root, new AlkemyPostorderReader<R, P>(conf));
        }

        public SingleTypeReader<R, P> postorder(int conf)
        {
            return new SingleTypeReader<R, P>(root, new AlkemyPostorderReader<R, P>(conf));
        }
    }
}
