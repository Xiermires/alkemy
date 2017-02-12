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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.agenttools.AgentTools;
import org.alkemy.parse.impl.AlkemizerCTF;
import org.alkemy.util.Measure;
import org.junit.BeforeClass;
import org.junit.Test;

// Alkemist usage examples.
public class AlkemistTest
{
    @BeforeClass
    public static void pre()
    {
        AgentTools.add(new AlkemizerCTF());
    }

    @Test
    public void testConcat()
    {
        final Alkemist alkemist = AlkemistFactory.create();
        final TestClass tc = new TestClass();
        final PropertyConcatenation concat = new PropertyConcatenation();
        alkemist.process(tc, concat);

        assertThat("0123456789", is(concat.get()));
    }

    @Test
    public void testObjectCopier()
    {
        final Alkemist alkemist = AlkemistFactory.create();
        final TestDeepCopy tdc = new TestDeepCopy();
        final TestClass tc = new TestClass();
        tc.s1 = "foo";
        tc.s2 = "bar";
        tdc.testClass = tc;

        final ObjectCopier<TestDeepCopy> copier = new ObjectCopier<TestDeepCopy>(new TestDeepCopy());
        alkemist.process(tdc, copier);

        assertThat(copier.get().testClass, is(not(nullValue())));
        assertThat(copier.get().testClass.s0, is("0"));
        assertThat(copier.get().testClass.s2, is("bar"));
        assertThat(copier.get().testClass.s1, is("foo"));
        assertThat(copier.get().testClass.s3, is("3"));
        assertThat(copier.get().testClass.s4, is("4"));
        assertThat(copier.get().testClass.s5, is("5"));
        assertThat(copier.get().testClass.s6, is("6"));
        assertThat(copier.get().testClass.s7, is("7"));
        assertThat(copier.get().testClass.s8, is("8"));
        assertThat(copier.get().testClass.s9, is("9"));
    }

    @Test
    public void peformance() throws Throwable
    {
        final AssignConstant<String> assign = new AssignConstant<String>("foo");

        final TestClass tc = new TestClass();
        final Alkemist alkemist = AlkemistFactory.create();

        System.out.println("Assign 1e7 strings: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(tc, assign);
            }
        }) / 1000000 + " ms");
    }
}
