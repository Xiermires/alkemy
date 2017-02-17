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
package org.alkemy.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.AbstractAlkemyProvider;
import org.alkemy.Alkemist;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Measure;
import org.alkemy.util.Node;
import org.junit.Test;

public class ObjectFactoryVisitorTest
{
    @Test
    public void testObjectFactoryVisitor()
    {
        final ObjectFactoryVisitor factory = new ObjectFactoryVisitor(new ConstantInt());
        Alkemist.process(new TestNested(), factory);

        TestNested tn = (TestNested) factory.get();
        assertThat(tn.i0, is(55));
        assertThat(tn.i1, is(55));
        assertThat(tn.a.i0, is(55));
        assertThat(tn.a.i1, is(55));
        assertThat(tn.a.b.i0, is(55));
        assertThat(tn.a.b.i1, is(55));
        assertThat(tn.b.i0, is(55));
        assertThat(tn.b.i1, is(55));
    }

    @Test
    public void performanceCreateNestedObjects() throws Throwable
    {
        final ObjectFactoryVisitor factory = new ObjectFactoryVisitor(new ConstantInt());

        System.out.println("Handle 4e6 nested objects, 8e6 indexed elements: " + Measure.measure(() ->
        {
            for (int i = 0; i < 100; i++) // FIXME The ObjectFactoryVisitor has a huge performance bug.
            {
                Alkemist.process(new TestNested(), factory);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceCreateFlatObjects() throws Throwable
    {
        final Node<AbstractAlkemyElement<?>> root = AlkemyParsers.fieldParser().parse(TestClass.class);

        System.out.println("Create 1e6 objects: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                root.data().newInstance(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
            }
        }) / 1000000 + " ms");
    }

    static class ConstantInt extends AbstractAlkemyProvider
    {
        @Override
        public Integer getInteger(Key key)
        {
            return 55;
        }
    }
}
