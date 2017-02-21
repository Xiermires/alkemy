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
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Measure;
import org.alkemy.util.Node;
import org.alkemy.util.PassThrough;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor;
import org.junit.Test;

// Alkemist usage examples.
public class AlkemistTest
{
    @Test
    public void testConcat()
    {
        final PropertyConcatenation concat = new PropertyConcatenation();
        final Alkemist alkemist = new AlkemistBuilder().visitor(concat).build();
        final TestClass tc = new TestClass();
        alkemist.process(tc);

        assertThat("01234", is(concat.get()));
    }

    @Test
    public void testAssign()
    {
        final Alkemist alkemist = new AlkemistBuilder().visitor(new AssignConstant<String>("bar")).build();

        final TestClass tc = new TestClass();
        alkemist.process(tc);

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
        final TestDeepCopy copy = Alkemist.process(tdc, copier, AlkemyParsers.fieldParser());

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
    public void testIterableProcess()
    {
        final TestClass tc1 = new TestClass();
        final TestClass tc2 = new TestClass();
        tc1.s0 = "foo";
        tc2.s1 = "bar";

        final Alkemist alkemist = new AlkemistBuilder().build(new ObjectCopier<TestDeepCopy>());

        final List<String> s0s1 = new ArrayList<>();
        for (TestClass tc : alkemist.iterable(Arrays.asList(tc1, tc2)))
        {
            s0s1.add(tc.s0);
            s0s1.add(tc.s1);
        }
        assertThat(s0s1, contains("foo", "1", "0", "bar"));
        
        final StringBuilder sb = new StringBuilder();
        alkemist.iterable(Arrays.asList(tc1, tc2)).forEach(e -> sb.append(e.s0).append(e.s1));
        assertThat(sb.toString(), is("foo10bar"));
    }

    @Test
    public void testIterableCreate()
    {
        final Supplier<Boolean> upTo100 = new Supplier<Boolean>()
        {
            int i = 0;

            @Override
            public Boolean get()
            {
                return i++ < 100;
            }
        };
        final Alkemist alkemist = new AlkemistBuilder().visitor(new AssignConstant<String>("foo")).build();

        final Set<TestClass> created = new HashSet<>();
        for (TestClass tc : alkemist.iterable(TestClass.class, upTo100))
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
    public void peformanceElementVisitor() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().visitor(new AssignConstant<String>("foo")).build();
        final TestClass tc = new TestClass();

        System.out.println("Assign 5e6 strings: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(tc);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void peformanceTypeVisitor() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().visitor(new PassThrough()).build();
        final TestClass tc = new TestClass();

        System.out.println("Visiting 1e6 types: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(tc);
            }
        }) / 1000000 + " ms");
    }
    
    @Test
    public void peformanceFastVisitorAssign() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().build(new FastVisitorConcept());
        final TestFastVisitor fvc = new TestFastVisitor();

        System.out.println("Fast visitor 1e7 assign: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(fvc);
            }
        }) / 1000000 + " ms");
    }
    
    @Test
    public void peformanceFastVisitorCreate() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().build(new FastVisitorConcept());

        System.out.println("Fast visitor 1e6 create (10 fields): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.delegateToNodeVisitor(TestFastVisitor.class);
            }
        }) / 1000000 + " ms");
    }
    
    // Clumsy impl. of a fast set / get visitor.
    // Takes advantage of static alkemization.
    static class FastVisitorConcept implements AlkemyNodeVisitor, AlkemyElementVisitor<IdxElement>
    {
        final String[] source = new String[] { "two", "one", "zero", "five", "four", "three", "six", "seven", "nine", "eight" };
        
        int map = 0;
        int arg = 0;
        IdxElement[] mapped = null;
        Object[] args;
        
        // create
        @Override
        public Object visit(Node<? extends AbstractAlkemyElement<?>> node)
        {
            args = args != null ? args : new Object[node.children().size()];
            
            if (mapped == null) // this happens once
            {
                mapped = new IdxElement[node.children().size()];
                node.children().forEach(c -> {
                    args[arg++] = c.data().accept(this);
                });
            }
            else // fast assign
            {
                for (int i=0; i<mapped.length; i++)
                {
                    args[i] = visit(mapped[i]);
                }
            }
            return node.data().newInstance(args);
        }
        
        // create
        @Override
        public Object visit(IdxElement element)
        {
            return source[element.idx];
        }
        
        // assign
        @Override
        public Object visit(Node<? extends AbstractAlkemyElement<?>> node, Object parent, Object... args)
        {
            if (mapped == null)
            {
                mapped = new IdxElement[node.children().size()];
                node.children().forEach(c -> {
                    c.data().accept(this, parent);
                });
            }
            else
            {
                for (int i=0; i<mapped.length; i++)
                {
                    visit(mapped[i], parent);
                }
            }
            return parent;
        }

        // assign
        @Override
        public void visit(IdxElement element, Object parent)
        {
            element.set(source[element.idx], parent);
        }
        
        @Override
        public boolean accepts(Class<?> type)
        {
            return Idx.class == type;
        }
        
        @Override
        public IdxElement map(AlkemyElement e)
        {
            return (mapped[map++] = new IdxElement(e));
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
}
