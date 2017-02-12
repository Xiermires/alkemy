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
import org.alkemy.alkemizer.AlkemizerCTF;
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
        
        assertThat("HelloWorld", is(concat.get()));
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
        assertThat(copier.get().testClass.s1, is("foo"));
        assertThat(copier.get().testClass.s2, is("bar"));
    }
}
