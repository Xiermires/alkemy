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
package org.alkemy.visitor.impl;

import static org.alkemy.visitor.impl.AbstractTraverser.INCLUDE_NULL_BRANCHES;
import static org.alkemy.visitor.impl.AbstractTraverser.INSTANTIATE_NODES;
import static org.alkemy.visitor.impl.AbstractTraverser.VISIT_NODES;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Stack;

import org.alkemy.Alkemy;
import org.alkemy.Alkemy.SingleTypeReader;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.util.AbstractAlkemyValueProvider;
import org.alkemy.util.Measure;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyValueProvider;
import org.alkemy.visitor.impl.AlkemyVisitorTests.ObjectReader.Bar;
import org.alkemy.visitor.impl.TestReader.NestedA;
import org.junit.Test;

public class AlkemyVisitorTests
{
    @Test
    public void testReadAnObject()
    {
        final ObjectReader or = new ObjectReader(new Stack<Integer>());
        Alkemy.mature(new TestReader(), or);
        assertThat(or.stack.size(), is(8));
    }

    @Test
    public void testWriteAnObjUsingPreorderVisitor()
    {
        final AlkemyPreorderReader<TestWriter, Object> aew = new AlkemyPreorderReader<>(INCLUDE_NULL_BRANCHES | INSTANTIATE_NODES);
        final TypifiedNode<TestWriter, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestWriter.class);
        final ObjectWriter ow = new ObjectWriter(new Constant<>(55));
        final TestWriter tw = TestWriter.class.cast(aew.create(ow, node));

        assertThat(tw.a, is(55));
        assertThat(tw.b, is(55));
        assertThat(tw.c, is(55));
        assertThat(tw.d, is(55));
        assertThat(tw.na.a, is(55));
        assertThat(tw.na.b, is(55));
        assertThat(tw.nb.c, is(55));
        assertThat(tw.nb.d, is(55));
    }

    @Test
    public void performanceWriteAnObjectUsingCustomWriter() throws Throwable
    {
        final AlkemyElementWriter<TestClass, Object> aew = new AlkemyElementWriter<>();
        final ObjectWriter ow = new ObjectWriter(new Constant<AlkemyElement>(55));
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);

        System.out.println("Create 1e6 objects (custom): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                aew.create(ow, node);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceWriteAnObjectUsingBulkReader() throws Throwable
    {
        final AlkemyFlatNodeReader<TestClass, Object, AlkemyElement> anr = new AlkemyFlatNodeReader<>(Alkemy.nodes().get(
                TestClass.class), f -> f);

        final ObjectWriter ow = new ObjectWriter(new Constant<AlkemyElement>(55));
        System.out.println("Create 1e6 objects (bulkreader): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anr.create(ow);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceWriteAnObjUsingPreorderVisitor() throws Throwable
    {
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);
        final ObjectWriter ow = new ObjectWriter(new Constant<AlkemyElement>(55));
        final AlkemyPreorderReader<TestClass, Object> apr = new AlkemyPreorderReader<TestClass, Object>(INSTANTIATE_NODES);

        System.out.println("Create 1e6 objects (preorder): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                apr.create(ow, node);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void testPreorder()
    {
        final NameStack<TestReader> ns = new NameStack<>();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;

        new AlkemyPreorderReader<TestReader, Object>(VISIT_NODES).accept(ns, Alkemy.nodes().get(TestReader.class), tr);

        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.na"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.d"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.c"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader"));
        assertTrue(ns.names.isEmpty());
    }

    @Test
    public void testPostorder()
    {
        final NameStack<TestReader> ns = new NameStack<>();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;

        new AlkemyPostorderReader<TestReader, Object>(VISIT_NODES).accept(ns, Alkemy.nodes().get(TestReader.class), tr);

        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.na"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.d"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.c"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.a"));
        assertTrue(ns.names.isEmpty());
    }

    @Test
    public void testPreorderIncludeNulls()
    {
        final NameStack<TestReader> ns = new NameStack<>();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;

        new AlkemyPreorderReader<TestReader, Object>(INCLUDE_NULL_BRANCHES | VISIT_NODES).accept(ns,
                Alkemy.nodes().get(TestReader.class), tr);

        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.na2"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedB.d"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedB.c"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.nb"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.na"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.d"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.c"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader"));
        assertTrue(ns.names.isEmpty());
    }

    @Test
    public void testPostorderIncludeNulls()
    {
        final NameStack<TestReader> ns = new NameStack<>();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;

        new AlkemyPostorderReader<TestReader, Object>(INCLUDE_NULL_BRANCHES | VISIT_NODES).accept(ns,
                Alkemy.nodes().get(TestReader.class), tr);

        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.na2"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.nb"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedB.d"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedB.c"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.na"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.d"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.c"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.a"));
        assertTrue(ns.names.isEmpty());
    }

    @Test
    public void testVisitorController()
    {
        final AlkemyTypeCounter<TestVisitorController> countAs = new AlkemyTypeCounter<>(A.class);
        final AlkemyTypeCounter<TestVisitorController> countBs = new AlkemyTypeCounter<>(B.class);
        final AlkemyVisitorController<TestVisitorController> avc = new AlkemyVisitorController<>(Arrays.asList(countAs, countBs));

        Alkemy.reader(TestVisitorController.class).preorder(0).accept(avc, new TestVisitorController());

        assertThat(countAs.counter, is(5));
        assertThat(countBs.counter, is(5));
    }

    @Test
    public void testBulkVisitor()
    {
        final AlkemyFlatNodeReader<TestVisitorController, Object, AlkemyElement> bulkVisitor = new AlkemyFlatNodeReader<>(Alkemy
                .nodes().get(TestVisitorController.class), f -> f);

        final AlkemyTypeCounter<TestVisitorController> countAs = new AlkemyTypeCounter<>(A.class);
        for (int i = 0; i < 100; i++)
        {
            bulkVisitor.accept(countAs, new TestVisitorController());
        }
        // AlkemyFlatNodeReader doesn't do any accept, we expect to count both A's and B's.
        assertThat(countAs.counter, is(1000));
    }

    @Test
    public void performanceBulkReader() throws Throwable
    {
        final AlkemyFlatNodeReader<TestVisitorController, Object, AlkemyElement> anr = new AlkemyFlatNodeReader<>(Alkemy.nodes()
                .get(TestVisitorController.class), f -> f);

        final AlkemyTypeCounter<TestVisitorController> countAs = new AlkemyTypeCounter<>(A.class);
        System.out.println("Measure traverser (bulk) 1e6 objects: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anr.accept(countAs, new TestVisitorController());
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performancePreorderTraverser() throws Throwable
    {
        final SingleTypeReader<TestVisitorController, TestVisitorController> anr = Alkemy.reader(TestVisitorController.class)
                .preorder(0);

        final AlkemyTypeCounter<TestVisitorController> countAs = new AlkemyTypeCounter<>(A.class);
        System.out.println("Measure traverser (preorder) 1e6 objects: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anr.accept(countAs, new TestVisitorController());
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performancePostorderTraverser() throws Throwable
    {
        final SingleTypeReader<TestVisitorController, TestVisitorController> anr = Alkemy.reader(TestVisitorController.class)
                .postorder(0);

        final AlkemyTypeCounter<TestVisitorController> countAs = new AlkemyTypeCounter<>(A.class);
        System.out.println("Measure traverser (postorder) 1e6 objects: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anr.accept(countAs, new TestVisitorController());
            }
        }) / 1000000 + " ms");
    }

    // Implements both supplier & consumer
    static class ObjectReader implements AlkemyElementVisitor<Object, AlkemyElement>
    {
        private Stack<Integer> stack;

        ObjectReader(Stack<Integer> stack)
        {
            this.stack = stack;
        }

        @Override
        public void visit(AlkemyElement e, Object parent)
        {
            stack.push(e.get(parent, Integer.class));
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return Bar.class == type;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD })
        @AlkemyLeaf(Bar.class)
        public @interface Bar
        {
        }
    }

    static class ObjectWriter implements AlkemyElementVisitor<Object, AlkemyElement>
    {
        private AlkemyValueProvider<AlkemyElement, ?> avp;

        ObjectWriter(AlkemyValueProvider<AlkemyElement, ?> avp)
        {
            this.avp = avp;
        }

        @Override
        public Object create(AlkemyElement e)
        {
            return avp.getValue(e, null);
        }

        @Override
        public void visit(AlkemyElement e, Object parent)
        {
            e.set(avp.getValue(e, null), parent);
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return Foo.class.equals(type);
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD })
        @AlkemyLeaf(Foo.class)
        public @interface Foo
        {
        }
    }

    // TestClass always int.
    static class Constant<E extends AbstractAlkemyElement<E>> extends AbstractAlkemyValueProvider<E, Object>
    {
        final int c;

        Constant(int c)
        {
            this.c = c;
        }

        @Override
        public Object getValue(E e, Object p)
        {
            return c;
        }
    }

    static class NameStack<P> implements AlkemyElementVisitor<P, AlkemyElement>
    {
        final Stack<String> names = new Stack<String>();

        @Override
        public void visit(AlkemyElement e, Object parent)
        {
            names.add(e.targetName());
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return Bar.class == type;
        }
    }

    static class AlkemyTypeCounter<P> implements AlkemyElementVisitor<P, AlkemyElement>
    {
        int counter = 0;
        Class<?> type;

        AlkemyTypeCounter(Class<?> type)
        {
            this.type = type;
        }

        @Override
        public void visit(AlkemyElement e, Object parent)
        {
            counter++;
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return this.type == type;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf
    static @interface A
    {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf
    static @interface B
    {

    }
}
