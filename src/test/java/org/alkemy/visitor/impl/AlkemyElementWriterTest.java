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

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.Alkemist;
import org.alkemy.AlkemistBuilder;
import org.alkemy.AlkemistBuilder.Mode;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Measure;
import org.alkemy.util.Node;
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.impl.AlkemyValueProvider.Key;
import org.junit.Test;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class AlkemyElementWriterTest
{
    private final Objenesis objenesis = new ObjenesisStd();

    @Test
    public void testAlkemyElementWriter()
    {
        final ObjectFactory of = new ObjectFactory(new Constant(55));
        final AlkemyElementWriter aew = new AlkemyElementWriter(of);
        final TestWriter tc = Alkemist.process(TestWriter.class, aew);

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
    public void performanceAlkemyElementWriter() throws Throwable
    {
        final Alkemist alkemist = new AlkemistBuilder().visitor(new ObjectFactory(new Constant(55))).build(Mode.WRITE);
        System.out.println("Create 1e6 objects (writer): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(TestClass.class);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceCreateUsingNewInstanceDirect() throws Throwable
    {
        final Node<AbstractAlkemyElement<?>> root = AlkemyParsers.fieldParser().parse(TestClass.class);
        final Constant provider = new Constant(55);

        System.out.println("Create 1e6 objects (AbstractAlkemyElement#newInstance(...)): "
                + Measure.measure(() ->
                {
                    for (int i = 0; i < 1000000; i++)
                    {
                        final Key key = provider.createKey(root.children().get(0).data());
                        root.data().newInstance(provider.getInteger(key), provider.getInteger(key), provider.getInteger(key),
                                provider.getInteger(key), provider.getInteger(key));
                    }
                }) / 1000000 + " ms");
    }

    @Test
    public void performanceCreateUseObjenesis() throws Throwable
    {
        final Node<AbstractAlkemyElement<?>> root = AlkemyParsers.fieldParser().parse(TestClass.class);
        final Constant provider = new Constant(55);
        
        System.out.println("Create 1e6 objects (objenesis): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                final TestClass rootObj = objenesis.newInstance(TestClass.class);
                final Key key = provider.createKey(root.children().get(0).data());
                root.children().forEach(c -> c.data().set(provider.getInteger(key), rootObj));
                
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceCreateUseReflection() throws Throwable
    {
        final Node<AbstractAlkemyElement<?>> root = AlkemyParsers.fieldParser().parse(TestClass.class);
        final Constant provider = new Constant(55);
        
        System.out.println("Create 1e6 objects (reflection): " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                final TestClass rootObj = TestClass.class.newInstance();
                final Key key = provider.createKey(root.children().get(0).data());
                root.children().forEach(c -> c.data().set(provider.getInteger(key), rootObj));
            }
        }) / 1000000 + " ms");
    }

    static class ObjectFactory implements AlkemyElementVisitor<AlkemyElement, Object>
    {
        private AlkemyValueProvider avp;

        ObjectFactory(AlkemyValueProvider avp)
        {
            this.avp = avp;
        }

        @Override
        public void visit(Reference<Object> ref, AlkemyElement e)
        {
            ref.set(avp.getValue(avp.createKey(e)));
        }

        @Override
        public void visit(Reference<Object> ref, AlkemyElement e, Object... args)
        {
            ref.set(e.newInstance(args));
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
