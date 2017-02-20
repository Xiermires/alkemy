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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Stack;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.Alkemist;
import org.alkemy.AlkemistBuilder;
import org.alkemy.AlkemistBuilder.Mode;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.util.Measure;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyValueProvider;
import org.alkemy.visitor.impl.TestReader.NestedA;
import org.junit.Test;

public class AlkemyVisitorTests
{
    @Test
    public void testReadAnObject()
    {
        final ObjectReader or = new ObjectReader(new Stack<Integer>());
        Alkemist.process(new TestReader(), AlkemyPreorderVisitor.create(or, false));

        assertThat(or.stack.size(), is(8));
    }

    @Test
    public void testWriteAnObjectUsingCustomWriter()
    {
        final TestWriter tc = Alkemist.create(TestWriter.class, new AlkemyElementWriter(new ObjectWriter(
                new Constant<AlkemyElement>(55))));

        assertThat(tc.a, is(55));
        assertThat(tc.b, is(55));
        assertThat(tc.c, is(55));
        assertThat(tc.d, is(55));
        assertThat(tc.na.a, is(55));
        assertThat(tc.na.b, is(55));
        assertThat(tc.nb.c, is(55));
        assertThat(tc.nb.d, is(55));
    }

    @Test
    public void testWriteAnObjUsingPreorderVisitor()
    {
        final TestWriter tc = Alkemist.create(TestWriter.class, new AlkemyPreorderVisitor(new ObjectWriter(
                new Constant<AlkemyElement>(55)), true));

        assertThat(tc.a, is(55));
        assertThat(tc.b, is(55));
        assertThat(tc.c, is(55));
        assertThat(tc.d, is(55));
        assertThat(tc.na.a, is(55));
        assertThat(tc.na.b, is(55));
        assertThat(tc.nb.c, is(55));
        assertThat(tc.nb.d, is(55));
    }

    @Test
    public void performanceWriteAnObjectUsingCustomWriter() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().visitor(new ObjectWriter(new Constant<AlkemyElement>(55))).build(
                Mode.WRITE);
        System.out.println("Create 1e6 objects (custom): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.create(TestClass.class);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceWriteAnObjUsingPreorderVisitor() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().build(new AlkemyPreorderVisitor(new ObjectWriter(
                new Constant<AlkemyElement>(55)), true));
        System.out.println("Create 1e6 objects (preorder): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.create(TestClass.class);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void testPreorder()
    {
        final NameStack ns = new NameStack();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;
        
        Alkemist.process(tr, AlkemyPreorderVisitor.create(ns, false));
        
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader$NestedA.a"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.na"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.d"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.c"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.b"));
        assertThat(ns.names.pop(), is("org.alkemy.visitor.impl.TestReader.a"));
        assertTrue(ns.names.isEmpty());
    }

    @Test
    public void testPostorder()
    {
        final NameStack ns = new NameStack();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;
        
        Alkemist.process(tr, AlkemyPostorderVisitor.create(ns, false));
        
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
        final NameStack ns = new NameStack();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;
        
        Alkemist.process(tr, AlkemyPreorderVisitor.create(ns, true));
        
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
        assertTrue(ns.names.isEmpty());
    }

    @Test
    public void testPostorderIncludeNulls()
    {
        final NameStack ns = new NameStack();
        final TestReader tr = new TestReader();
        tr.na = new NestedA();
        tr.na2 = null;
        tr.nb = null;
        
        Alkemist.process(new TestReader(), AlkemyPostorderVisitor.create(ns, true));
        
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

    // Implements both supplier & consumer
    static class ObjectReader implements AlkemyElementVisitor<AlkemyElement>
    {
        private Stack<Integer> stack;

        ObjectReader(Stack<Integer> stack)
        {
            this.stack = stack;
        }

        @Override
        public void visit(AlkemyElement e, Object parent)
        {
            if (!e.isNode()) stack.push(Integer.valueOf((int) e.get(parent)));
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return ObjectReader.class.equals(type);
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD })
        @AlkemyLeaf(ObjectReader.class)
        public @interface Bar
        {
        }
    }

    static class ObjectWriter implements AlkemyElementVisitor<AlkemyElement>
    {
        private AlkemyValueProvider<AlkemyElement> avp;

        ObjectWriter(AlkemyValueProvider<AlkemyElement> avp)
        {
            this.avp = avp;
        }

        @Override
        public Object visit(AlkemyElement e)
        {
            return avp.getValue(e);
        }

        @Override
        public void visit(AlkemyElement e, Object parent)
        {
            if (e.isNode())
            {
                e.set(e.newInstance(), parent);
            }
            else
            {
                e.set(avp.getValue(e), parent);
            }
        }

        @Override
        public Object visit(AlkemyElement e, Object... args)
        {
            return e.newInstance(args);
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return ObjectWriter.class.equals(type);
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD })
        @AlkemyLeaf(ObjectWriter.class)
        public @interface Foo
        {
        }
    }

    // TestClass always int.
    static class Constant<E extends AbstractAlkemyElement<E>> extends AbstractAlkemyValueProvider<E>
    {
        final int c;

        Constant(int c)
        {
            this.c = c;
        }

        @Override
        public Integer getInteger(E key)
        {
            return c;
        }
    }

    static class NameStack implements AlkemyElementVisitor<AlkemyElement>
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
            return ObjectReader.class.equals(type);
        }
    }
}
