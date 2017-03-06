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

import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.agenttools.AgentTools;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AlkemizerCTF;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeHandler;
import org.alkemy.visitor.AlkemyNodeHandler.Entry;
import org.alkemy.visitor.AlkemyNodeReader;
import org.alkemy.visitor.impl.AlkemyPostorderReader;
import org.alkemy.visitor.impl.AlkemyPreorderReader;
import org.alkemy.visitor.impl.NodeReaderToVisitorAdapter;

/**
 * The Alkemy library allows applying user specific {@link AlkemyNodeReader},
 * {@link AlkemyNodeHandler} and {@link AlkemyElementVisitor} strategies to a set of alkemy
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

    private static NodeFactory nodes = nodes();

    /**
     * Clears the default {@link NodeFactory}.
     * <p>
     * This might be necessary in some scenarios. See
     * {@link AbstractAlkemyElement#useMappedRefCaching}.
     */
    public static void clearCache()
    {
        nodes = nodes();
    }

    /** 
     * As {@link #nodes(AlkemyParser)} but using the {@link AlkemyParsers#typeParser()} by default.
     */
    public static NodeFactory nodes()
    {
        return new AlkemyNodes(AlkemyParsers.typeParser());
    }

    /**
     * The node factory is the starting point of any alkemizing process.
     * <p>
     * Types can be converted into directed rooted trees of alkemy elements, which can be later feed
     * to any {@link AlkemyNodeHandler} and {@link AlkemyNodeReader} implementations.
     * <p>
     * Nodes are created using an {@link AlkemyParser} which is responsible for finding defined
     * alkemizations and creating alkemy elements out of a type.
     */
    public static NodeFactory nodes(AlkemyParser parser)
    {
        return new AlkemyNodes(parser);
    }

    /**
     * Simple alkemy method to parse, create an object and apply the provided visitor to all its
     * leaves.
     * <p>
     * Simple alkemy methods are provided as a quick access to some common, non-intensive uses. But
     * should be avoided in favor of creating specialized facades for the {@link AlkemyNodeHandler}s
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
        return new AlkemyPreorderReader<R, P>(INSTANTIATE_NODES).create(aev, nodes.get(r));
    }

    /**
     * As {@link #mature(Class, AlkemyElementVisitor)} but including a parameter.
     */
    public static <R, P> R mature(Class<R> r, AlkemyElementVisitor<P, ?> aev, P p)
    {
        return new AlkemyPreorderReader<R, P>(INSTANTIATE_NODES).create(aev, nodes.get(r), p);
    }

    /**
     * Simple alkemy method to parse an object type and apply the provided visitor to all leaves in
     * non-null branches.
     * <p>
     * Simple alkemy methods are provided as a quick access to some common, non-intensive uses. But
     * should be avoided in favor of creating specialized facades for the {@link AlkemyNodeHandler}s
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
    public static <R, P> R mature(R r, AlkemyElementVisitor<P, ?> aev)
    {
        return new AlkemyPreorderReader<R, P>(0).accept(aev, nodes.get((Class<R>) r.getClass()), r);
    }

    /**
     * As {@link #mature(Object, AlkemyElementVisitor)} but including a parameter.
     */
    @SuppressWarnings("unchecked")
    // safe
    public static <R, P> R mature(R r, P p, AlkemyElementVisitor<P, ?> aev)
    {
        return new AlkemyPreorderReader<R, P>(0).accept(aev, nodes.get((Class<R>) r.getClass()), r, p);
    }

    public static <R> ReaderFactory<R, R> reader(Class<R> retType)
    {
        return new ReaderFactory<R, R>(nodes.get(retType));
    }

    public static <R, P> ReaderFactory<R, P> reader(Class<R> retType, Class<P> paramType)
    {
        return new ReaderFactory<R, P>(nodes.get(retType));
    }

    public static interface NodeFactory
    {
        <R> TypifiedNode<R, ? extends AbstractAlkemyElement<?>> get(Class<R> type);
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
    
    /**
     * Part of the simple Alkemy syntax sugar. See {@link Alkemy}.
     * <p>
     * This class reproduces all Alkemy operations removing the Node parameter from the signature.
     */
    public static class SingleTypeReader<R, P>
    {
        private final TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root;
        private final AlkemyNodeReader<R, P> anv;

        SingleTypeReader(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root, AlkemyNodeReader<R, P> anv)
        {
            this.root = root;
            this.anv = anv;
        }

        /**
         * Generates an element of type R.
         */
        public R create(AlkemyElementVisitor<?, ?> aev)
        {
            return anv.create(aev, root);
        }

        /**
         * Generates an element of type R using a parameter P.
         */
        public R create(AlkemyElementVisitor<P, ?> aev, P parameter)
        {
            return anv.create(aev, root, parameter);
        }

        /**
         * Generates an element of type R, or modifies and returns the received param1 of type R.
         */
        public R accept(AlkemyElementVisitor<?, ?> aev, R parameter)
        {
            return anv.accept(aev, root, parameter);
        }
        
        /**
         * Generates an element of type R, or modifies and returns the received param1 of type R, using
         * the param2 of type P.
         */
        public R accept(AlkemyElementVisitor<P, ?> aev, R param1, P param2)
        {
            return anv.accept(aev, root, param1, param2);
        }
        
        /* * STREAM SUPPORT * */

        /**
         * Stream of {@link #iterable(TypifiedNode, Iterable)}
         */
        public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Iterable<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #iterable(TypifiedNode, Iterator)}
         */
        public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Iterator<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #peekIterable(TypifiedNode, Iterable)}
         */
        public Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #iterable(TypifiedNode, Iterator)}
         */
        public Stream<Entry<R, P>> peekStream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), -1, 0), false);
        }

        /**
         * Stream of {@link #iterable(TypifiedNode, Supplier)}
         */
        public Stream<R> stream(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, hasNext).iterator(), -1, 0), false);
        }

        /* * PARALLEL STREAM SUPPORT * */

        /**
         * Parallel stream of {@link #iterable(TypifiedNode, Iterable)}
         */
        public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Iterable<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #iterable(TypifiedNode, Iterator)}
         */
        public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Iterator<R> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #peekIterable(TypifiedNode, Iterable)}
         */
        public Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #peekIterable(TypifiedNode, Iterator)}
         */
        public Stream<Entry<R, P>> parallelPeekStream(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
        {
            return StreamSupport.stream(Spliterators.spliterator(peekIterable(aev, items).iterator(), Long.MAX_VALUE, 0), false);
        }

        /**
         * Parallel stream of {@link #iterable(TypifiedNode, Supplier)}
         */
        public Stream<R> parallelStream(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
        {
            return StreamSupport.stream(Spliterators.spliterator(iterable(aev, hasNext).iterator(), Long.MAX_VALUE, 0), false);
        }

        /* * ITERABLE SUPPORT * */

        /**
         * See {@link AlkemyNodeHandler#iterable(Node, Iterable)}
         */
        public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Iterable<R> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#iterable(Node, Iterator)}
         */
        public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Iterator<R> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#peekIterable(Node, Iterable)}
         */
        public Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, Iterable<P> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).peekIterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#peekIterable(Node, Iterator)}
         */
        public Iterable<Entry<R, P>> peekIterable(AlkemyElementVisitor<P, ?> aev, Iterator<P> items)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).peekIterable(root, items);
        }

        /**
         * See {@link AlkemyNodeHandler#iterable(Node, Supplier, Class)}
         */
        public Iterable<R> iterable(AlkemyElementVisitor<P, ?> aev, Supplier<Boolean> hasNext)
        {
            return new NodeReaderToVisitorAdapter<R, P>(anv, aev).iterable(root, hasNext);
        }
    }
}
