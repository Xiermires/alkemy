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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.Alkemist;
import org.alkemy.AlkemistBuilder;
import org.alkemy.AlkemistBuilder.Mode;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.util.Measure;
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.junit.Test;

public class AlkemyElementWriterTest
{
    @Test
    public void testAlkemyElementWriter()
    {
        final ObjectFactory of = new ObjectFactory(new Constant(55));
        final AlkemyElementWriter aew = new AlkemyElementWriter(of, of, false);
        
        final TestClass tc =  Alkemist.process(TestClass.class, aew);
        assertThat(tc.a, is(55));
        assertThat(tc.b, is(55));
    }

    @Test
    public void performanceAlkemyElementWriter() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().visitor(new ObjectFactory(new Constant(55))).build(Mode.WRITE);
        System.out.println("Create 1e6 objects: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(TestClass.class); 
            }
        }) / 1000000 + " ms");
    }

    // Implements both supplier & consumer
    static class ObjectFactory implements AlkemyElementVisitor<AlkemyElement, Object>
    {
        private AlkemyValueProvider avp;

        ObjectFactory(AlkemyValueProvider avp)
        {
            this.avp = avp;
        }

        @Override
        public void visit(AlkemyElement e, Reference<Object> ref, Object... params)
        {
            if (params.length > 0) // consumer mode (generate object)
            {
                ref.set(e.newInstance(params));
            }
            else
            // supplier mode, update the reference
            {
                ref.set(avp.getValue(avp.createKey(e)));
            }
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return ObjectFactory.class.equals(type);
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD })
        @AlkemyLeaf(ObjectFactory.class)
        public @interface Foo
        {
        }
    }

    // TestClass always int.
    static class Constant extends AbstractAlkemyValueProvider
    {
        final int c;

        Constant(int c)
        {
            this.c = c;
        }

        @Override
        public Integer getInteger(Key key)
        {
            return c;
        }
    }
}
