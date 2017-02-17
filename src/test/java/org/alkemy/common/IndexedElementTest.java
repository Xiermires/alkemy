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

import java.util.Properties;

import org.alkemy.Alkemist;
import org.alkemy.AlkemistBuilder;
import org.alkemy.util.Measure;
import org.junit.Test;

public class IndexedElementTest
{
    @Test
    public void testIndexedElement()
    {
        final Properties m = new Properties();
        final Alkemist alkemist = new AlkemistBuilder().visitor(new IndexedElementVisitor((a, b) -> m.put(a, b))).build();
        final TestClass tc = new TestClass();

        alkemist.process(tc);

        assertThat(m, hasEntry(0, 4));
        assertThat(m, hasEntry(1, 3));
        assertThat(m, hasEntry(2, 2));
        assertThat(m, hasEntry(3, 1));
        assertThat(m, hasEntry(4, 0));
    }

    @Test
    public void performanceIndexed() throws Throwable
    {
        final Properties m = new Properties();
        final Alkemist alkemist = new AlkemistBuilder().visitor(new IndexedElementVisitor((a, b) -> m.put(a, b))).build();
        final TestClass tc = new TestClass();

        System.out.println("Handle 5e6 indexed elements: " + Measure.measure(() ->
        {
            for (int i = 0; i < 1000000; i++)
            {
                alkemist.process(tc);
            }
        }) / 1000000 + " ms");
    }
}
