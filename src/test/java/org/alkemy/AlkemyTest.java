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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.Measure;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.util.PassThrough;
import org.alkemy.visitor.AlkemyNodeVisitor.FluentAlkemyNodeVisitor;
import org.alkemy.visitor.impl.AlkemyPreorderReader.FluentAlkemyPreorderReader;
import org.junit.Test;

// Alkemy general usage examples.
public class AlkemyTest
{
    @Test
    public void testConcat()
    {
        final PropertyConcatenation<TestClass> concat = new PropertyConcatenation<>();
        Alkemy.mature(new TestClass(), concat);
        
        assertThat("01234", is(concat.get()));
    }

    @Test
    public void testAssign()
    {
        final TestClass tc = Alkemy.mature(new TestClass(), new AssignConstant<>("bar"));
        
        assertThat(tc.s0, is("0"));
        assertThat(tc.s1, is("1"));
        assertThat(tc.s2, is("2"));
        assertThat(tc.s3, is("3"));
        assertThat(tc.s4, is("4"));
        assertThat(tc.s5, is("bar"));
        assertThat(tc.s6, is("bar"));
        assertThat(tc.s7, is("bar"));
        assertThat(tc.s8, is("bar"));
        assertThat(tc.s9, is("bar"));
    }

    @Test
    public void testObjectCopier()
    {
        final TestDeepCopy tdc = new TestDeepCopy();
        final TestClass tc = new TestClass();
        tc.s1 = "foo";
        tc.s2 = "bar";
        tdc.testClass = tc;

        final ObjectCopier<TestDeepCopy> copier = new ObjectCopier<>();
        final TestDeepCopy copy = copier.visit(Alkemy.nodes().get(TestDeepCopy.class), tdc);

        assertThat(copy.testClass, is(not(nullValue())));
        assertThat(copy.testClass.s0, is("0"));
        assertThat(copy.testClass.s1, is("foo"));
        assertThat(copy.testClass.s2, is("bar"));
        assertThat(copy.testClass.s3, is("3"));
        assertThat(copy.testClass.s4, is("4"));
        assertThat(copy.testClass.s5, is("5"));
        assertThat(copy.testClass.s6, is("6"));
        assertThat(copy.testClass.s7, is("7"));
        assertThat(copy.testClass.s8, is("8"));
        assertThat(copy.testClass.s9, is("9"));
    }

    @Test
    public void testFluentIterable()
    {
        final TestClass tc1 = new TestClass();
        final TestClass tc2 = new TestClass();
        tc1.s0 = "foo";
        tc2.s1 = "bar";
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);
        final ObjectCopier<TestClass> oc = new ObjectCopier<TestClass>();

        final List<String> s0s1 = new ArrayList<>();
        for (TestClass tc : oc.iterable(node, Arrays.asList(tc1, tc2)))
        {
            s0s1.add(tc.s0);
            s0s1.add(tc.s1);
        }

        assertThat(s0s1, contains("foo", "1", "0", "bar"));
        final StringBuilder sb = new StringBuilder();
        oc.iterable(node, Arrays.asList(tc1, tc2)).forEach(e -> sb.append(e.s0).append(e.s1));
        assertThat(sb.toString(), is("foo10bar"));

    }

    @Test
    public void testCreateIterable()
    {
        final FluentAlkemyPreorderReader<TestClass> anv = new FluentAlkemyPreorderReader<TestClass>(0);
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);
        final AssignConstant<TestClass, String> aev = new AssignConstant<>("foo");
        final Supplier<Boolean> upTo100 = new Supplier<Boolean>()
        {
            int i = 0;

            @Override
            public Boolean get()
            {
                return i++ < 100;
            }
        };

        final Set<TestClass> created = new HashSet<>();
        for (TestClass tc : anv.iterable(aev, node, upTo100))
        {
            created.add(tc);
        }

        assertThat(created.size(), is(100));
        for (TestClass tc : created)
        {
            assertThat(tc.s0, is("0"));
            assertThat(tc.s1, is("1"));
            assertThat(tc.s2, is("2"));
            assertThat(tc.s3, is("3"));
            assertThat(tc.s4, is("4"));
            assertThat(tc.s5, is("foo"));
            assertThat(tc.s6, is("foo"));
            assertThat(tc.s7, is("foo"));
            assertThat(tc.s8, is("foo"));
            assertThat(tc.s9, is("foo"));
        }
    }

    @Test
    public void testStreamForEach()
    {
        final FluentAlkemyPreorderReader<TestClass> anv = new FluentAlkemyPreorderReader<TestClass>(0);
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);
        final AssignConstant<TestClass, String> aev = new AssignConstant<>("foo");

        final Set<TestClass> created = new HashSet<>();
        anv.stream(aev, node, upTo100()).forEach(c -> created.add(c));

        assertThat(created.size(), is(100));
        for (TestClass tc : created)
        {
            assertThat(tc.s0, is("0"));
            assertThat(tc.s1, is("1"));
            assertThat(tc.s2, is("2"));
            assertThat(tc.s3, is("3"));
            assertThat(tc.s4, is("4"));
            assertThat(tc.s5, is("foo"));
            assertThat(tc.s6, is("foo"));
            assertThat(tc.s7, is("foo"));
            assertThat(tc.s8, is("foo"));
            assertThat(tc.s9, is("foo"));
        }
    }

    @Test
    public void testStreamFilter()
    {
        final FluentAlkemyPreorderReader<TestClass> anv = new FluentAlkemyPreorderReader<TestClass>(0);
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);
        final AssignConstant<TestClass, String> aev = new AssignConstant<>("foo");
        final Set<TestClass> created = new HashSet<>();
        // s0.equals("0") always
        anv.stream(aev, node, upTo100()).filter(f -> !f.s0.equals("0")).forEach(c -> created.add(c));
        assertThat(created.size(), is(0));

        anv.stream(aev, node, upTo100()).filter(f -> f.s0.equals("0")).forEach(c -> created.add(c));
        assertThat(created.size(), is(100));
    }

    @Test
    public void peformanceElementVisitor() throws Throwable
    {
        final FluentAlkemyPreorderReader<TestClass> anv = new FluentAlkemyPreorderReader<>(0);
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);
        final AssignConstant<TestClass, String> aev = new AssignConstant<>("foo");
        final TestClass tc = new TestClass();

        System.out.println("Assign 5e6 strings: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anv.accept(aev, node, tc);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void peformanceElementVisitorNoInstr() throws Throwable
    {
        final FluentAlkemyPreorderReader<TestClassNoInstr> anv = new FluentAlkemyPreorderReader<>(0);
        final TypifiedNode<TestClassNoInstr, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes()
                .get(TestClassNoInstr.class);
        final AssignConstant<TestClassNoInstr, String> aev = new AssignConstant<>("foo");
        final TestClassNoInstr tc = new TestClassNoInstr(); // do not include in the suite.

        System.out.println("Assign 5e6 strings: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anv.accept(aev, node, tc);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void peformanceTypeVisitor() throws Throwable
    {
        final FluentAlkemyPreorderReader<TestClass> anv = new FluentAlkemyPreorderReader<>(0);
        final TypifiedNode<TestClass, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestClass.class);
        final PassThrough<TestClass> aev = new PassThrough<>();
        final TestClass tc = new TestClass();

        System.out.println("Visiting 1e6 types: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anv.accept(aev, node, tc);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void peformanceFastVisitorAssign() throws Throwable
    {
        final TypifiedNode<TestFastVisitor, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestFastVisitor.class);
        final FastSameFlatObjConcept<TestFastVisitor> fsfoc = new FastSameFlatObjConcept<>();
        final TestFastVisitor fvc = new TestFastVisitor();

        System.out.println("Fast visitor 1e7 assign: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                fsfoc.visit(node, fvc);
            }
        }) / 1000000 + " ms");
    }
    
    @Test
    public void peformanceFastVisitorAssignUsingParallelStream() throws Throwable
    {
        final TypifiedNode<TestFastVisitor, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestFastVisitor.class);
        final FastSameFlatObjConcept<TestFastVisitor> anv = new FastSameFlatObjConcept<>();
        final TestFastVisitor fvc = new TestFastVisitor();

        final Stream<TestFastVisitor> stream = anv.parallelStream(node, new InstanceProviderIterator<TestFastVisitor>(1000000, () -> fvc));
        
        System.out.println("Fast visitor 1e7 assign (parallel stream): " + Measure.measure(() ->
        {
            stream.forEach(AlkemyTest::sink);
        }) / 1000000 + " ms");
    }
    
    static <R> void sink(R r)
    {   
    }

    @Test
    public void peformanceFastVisitorCreate() throws Throwable
    {
        final TypifiedNode<TestFastVisitor, ? extends AbstractAlkemyElement<?>> node = Alkemy.nodes().get(TestFastVisitor.class);
        final FastSameFlatObjConcept<TestFastVisitor> anv = new FastSameFlatObjConcept<>();

        System.out.println("Fast visitor 1e6 create (10 fields): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                anv.visit(node);
            }
        }) / 1000000 + " ms");
    }

    private Supplier<Boolean> upTo100()
    {
        return new Supplier<Boolean>()
        {
            int i = 0;

            @Override
            public Boolean get()
            {
                return i++ < 100;
            }
        };
    }

    // Fast impl. of a fast set / get.
    // Takes advantage of static alkemization.
    static class FastSameFlatObjConcept<R> implements FluentAlkemyNodeVisitor<R>
    {
        // The source provider
        private final String[] source = new String[] { "two", "one", "zero", "five", "four", "three", "six", "seven", "nine",
                "eight" };

        private IdxElement[] mapped = null;
        private Object[] args;

        // create
        @Override
        public R visit(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node)
        {
            args = args != null ? args : new Object[node.children().size()];
            if (mapped == null) // map once
            {
                mapped = map(node);
            }
            for (int i = 0; i < mapped.length; i++)
            {
                args[i] = source[mapped[i].idx];
            }
            return node.data().safeNewInstance(node.type(), args);
        }

        // assign
        @Override
        public R visit(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, R parent)
        {
            if (mapped == null)
            {
                mapped = map(node);
            }

            for (int i = 0; i < mapped.length; i++)
            {
                mapped[i].set(source[mapped[i].idx], parent);
            }
            return parent;
        }

        private IdxElement[] map(Node<? extends AbstractAlkemyElement<?>> node)
        {
            final IdxElement[] mapped = new IdxElement[node.children().size()];
            for (int i = 0; i < mapped.length; i++)
            {
                mapped[i] = new IdxElement(node.children().get(i).data());
            }
            return mapped;
        }
    }

    static class IdxElement extends AbstractAlkemyElement<IdxElement>
    {
        int idx;

        protected IdxElement(AbstractAlkemyElement<?> other)
        {
            super(other);
            idx = other.desc().getAnnotation(Idx.class).value();
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf(Idx.class)
    static @interface Idx
    {
        int value();
    }
    
    static class InstanceProviderIterator<R> implements Iterator<R>
    {
        private int i = 0;
        private int n;
        private Supplier<R> s;
        
        InstanceProviderIterator(int n, Supplier<R> s)
        {
            this.n = n;
            this.s = s;
        }
        
        @Override
        public boolean hasNext()
        {
            return i++ < n;
        }

        @Override
        public R next()
        {
            return s.get();
        }
    }
}
