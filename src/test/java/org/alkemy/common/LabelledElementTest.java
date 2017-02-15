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

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alkemy.Alkemist;
import org.alkemy.AlkemistBuilder;
import org.alkemy.util.Measure;
import org.junit.Test;

public class LabelledElementTest
{
    @Test
    public void testStaticLabels()
    {
        final Properties props = new Properties();
        final Alkemist alkemist = new AlkemistBuilder().visitor(new LabelledElementVisitor((a, b) -> props.put(a, b))).build();
        final TestClass tc = new TestClass();

        alkemist.process(tc);

        assertThat(props, hasEntry("id0", 4));
        assertThat(props, hasEntry("id1", 3));
        assertThat(props, hasEntry("id2", 2));
        assertThat(props, hasEntry("id3", 1));
        assertThat(props, hasEntry("id4", 0));
    }

    @Test
    public void testDynamicLabels()
    {
        final Properties props = new Properties();
        final Map<String, String> dynParams = new HashMap<>();
        dynParams.put("dyn1", "id10");
        dynParams.put("prefix", "aaa");
        dynParams.put("infix", "ccc");
        dynParams.put("suffix", "eee");
        final LabelledElementVisitor aev = new LabelledElementVisitor((a, b) -> props.put(a, b)).dynamicVariables(dynParams);
        final Alkemist alkemist = new AlkemistBuilder().visitor(aev).build();
        final TestClass tc = new TestClass();

        alkemist.process(tc);

        assertThat(props, hasEntry("id10", 5));
        assertThat(props, hasEntry("aaa.bbb.ccc.ddd.eee", 6));
    }

    @Test
    public void performanceLabelled() throws Throwable
    {
        final Map<String, Integer> m = new HashMap<String, Integer>();
        final Alkemist alkemist = new AlkemistBuilder().visitor(new LabelledElementVisitor((a, b) -> m.put(a, (Integer) b)))
                .build();
        final TestClass tc = new TestClass();

        System.out.println("Handle 5e6 labelled elements: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(tc);
            }
        }) / 1000000 + " ms");
    }
}
