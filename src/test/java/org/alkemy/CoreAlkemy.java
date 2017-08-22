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
package org.alkemy;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.alkemy.parse.impl.BarElement;
import org.alkemy.util.Node;
import org.junit.Test;

public class CoreAlkemy
{
    @Test
    public void set()
    {
        final TestClass tc = new TestClass();
        AlkemyNodes.get(TestClass.class).forEach(e -> e.set(-1, tc));

        assertThat(tc.n1, is(-1));
        assertThat(tc.n2, is(-1));
        assertThat(tc.n3, is(-1));
        assertThat(tc.n4, is(-1));
        assertThat(tc.n5, is(-1));
    }

    @Test
    public void get()
    {
        final TestClass tc = new TestClass();
        final Summation sum = new Summation();
        AlkemyNodes.get(TestClass.class).stream().filter(c -> !c.isNode()).forEach(c -> sum.add(c.get(tc, Integer.class)));

        assertThat(sum.sum, is(15));
    }

    @Test
    public void transformTree()
    {
        final TestClass tc = new TestClass();
        final Summation sum = new Summation();
        final Node<BarElement> root = AlkemyNodes.get(TestClass.class, p -> p.alkemyType() == Bar.class, f -> new BarElement(f));
        root.stream().filter(c -> !c.isNode()).forEach(c -> sum.add(c.get(tc, Integer.class)));

        assertThat(sum.sum, is(9));
    }

    @Test
    public void preorder()
    {
        final List<String> elements = new ArrayList<String>();
        AlkemyNodes.get(TestTraverse.class).forEach(e -> elements.add(e.valueName()));

        assertThat(elements, hasSize(23));
        assertThat(elements.get(0), is("org.alkemy.TestTraverse"));
        assertThat(elements.get(1), is("org.alkemy.TestTraverse.a"));
        assertThat(elements.get(2), is("org.alkemy.TestTraverse.b"));
        assertThat(elements.get(3), is("org.alkemy.TestTraverse.c"));
        assertThat(elements.get(4), is("org.alkemy.TestTraverse.d"));
        assertThat(elements.get(5), is("org.alkemy.TestTraverse.na"));
        assertThat(elements.get(6), is("org.alkemy.TestTraverse$NestedA.a1"));
        assertThat(elements.get(7), is("org.alkemy.TestTraverse$NestedA.nad"));
        assertThat(elements.get(8), is("org.alkemy.TestTraverse$NestedD.d1"));
        assertThat(elements.get(9), is("org.alkemy.TestTraverse$NestedD.d2"));
        assertThat(elements.get(10), is("org.alkemy.TestTraverse$NestedA.a2"));
        assertThat(elements.get(11), is("org.alkemy.TestTraverse.nb"));
        assertThat(elements.get(12), is("org.alkemy.TestTraverse$NestedB.b1"));
        assertThat(elements.get(13), is("org.alkemy.TestTraverse$NestedB.b2"));
        assertThat(elements.get(14), is("org.alkemy.TestTraverse$NestedB.nbe"));
        assertThat(elements.get(15), is("org.alkemy.TestTraverse$NestedE.e1"));
        assertThat(elements.get(16), is("org.alkemy.TestTraverse$NestedE.e2"));
        assertThat(elements.get(17), is("org.alkemy.TestTraverse.nc"));
        assertThat(elements.get(18), is("org.alkemy.TestTraverse$NestedC.ncf"));
        assertThat(elements.get(19), is("org.alkemy.TestTraverse$NestedF.f1"));
        assertThat(elements.get(20), is("org.alkemy.TestTraverse$NestedF.f2"));
        assertThat(elements.get(21), is("org.alkemy.TestTraverse$NestedC.c1"));
        assertThat(elements.get(22), is("org.alkemy.TestTraverse$NestedC.c2"));
    }

    @Test
    public void postorder()
    {
        final List<String> elements = new ArrayList<String>();
        AlkemyNodes.get(TestTraverse.class).postorder().forEach(e -> elements.add(e.valueName()));

        assertThat(elements, hasSize(23));
        assertThat(elements.get(0), is("org.alkemy.TestTraverse.a"));
        assertThat(elements.get(1), is("org.alkemy.TestTraverse.b"));
        assertThat(elements.get(2), is("org.alkemy.TestTraverse.c"));
        assertThat(elements.get(3), is("org.alkemy.TestTraverse.d"));
        assertThat(elements.get(4), is("org.alkemy.TestTraverse$NestedA.a1"));
        assertThat(elements.get(5), is("org.alkemy.TestTraverse$NestedD.d1"));
        assertThat(elements.get(6), is("org.alkemy.TestTraverse$NestedD.d2"));
        assertThat(elements.get(7), is("org.alkemy.TestTraverse$NestedA.nad"));
        assertThat(elements.get(8), is("org.alkemy.TestTraverse$NestedA.a2"));
        assertThat(elements.get(9), is("org.alkemy.TestTraverse.na"));
        assertThat(elements.get(10), is("org.alkemy.TestTraverse$NestedB.b1"));
        assertThat(elements.get(11), is("org.alkemy.TestTraverse$NestedB.b2"));
        assertThat(elements.get(12), is("org.alkemy.TestTraverse$NestedE.e1"));
        assertThat(elements.get(13), is("org.alkemy.TestTraverse$NestedE.e2"));
        assertThat(elements.get(14), is("org.alkemy.TestTraverse$NestedB.nbe"));
        assertThat(elements.get(15), is("org.alkemy.TestTraverse.nb"));
        assertThat(elements.get(16), is("org.alkemy.TestTraverse$NestedF.f1"));
        assertThat(elements.get(17), is("org.alkemy.TestTraverse$NestedF.f2"));
        assertThat(elements.get(18), is("org.alkemy.TestTraverse$NestedC.ncf"));
        assertThat(elements.get(19), is("org.alkemy.TestTraverse$NestedC.c1"));
        assertThat(elements.get(20), is("org.alkemy.TestTraverse$NestedC.c2"));
        assertThat(elements.get(21), is("org.alkemy.TestTraverse.nc"));
        assertThat(elements.get(22), is("org.alkemy.TestTraverse"));
    }

    @Test
    public void testStaticGet()
    {
        final TestStatic ts = new TestStatic();
        final Summation sum = new Summation();
        AlkemyNodes.get(TestStatic.class).stream().filter(c -> !c.isNode() && c.alkemyType() == Foo.class)//
        .forEach(c -> sum.add(c.getInt(ts)));

        assertThat(sum.sum, is(1));
    }
    
    @Test
    public void testStaticSet()
    {
        AlkemyNodes.get(TestStatic.class).stream().filter(c -> !c.isNode() && c.alkemyType() == Foo.class)//
        .forEach(c -> c.set(5, null));

        final Summation sum = new Summation();
        AlkemyNodes.get(TestStatic.class).stream().filter(c -> !c.isNode() && c.alkemyType() == Foo.class)//
        .forEach(c -> sum.add(c.getInt(null)));

        assertThat(sum.sum, is(5));
    }

    public static class Summation
    {
        int sum = 0;

        void add(int i)
        {
            sum += i;
        }
    }
}
