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
package org.alkemy.parse.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alkemy.TestClass;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AlkemyElement;
import org.alkemy.parse.impl.AlkemyParsers;
import org.junit.Test;

public class TypeParserTest
{
    @Test
    public void parseTestClass()
    {
        final AlkemyParser parser = AlkemyParsers.typeParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestClass.class).drainTo(result);

        assertThat(result.size(), is(6));

        final TestClass tc = new TestClass();
        assertThat(1 + 2 + 3 + 4 + 5, is(result.stream().filter(d -> !d.isNode()).mapToInt(d -> (int) d.get(tc)).sum()));
    }

    @Test
    public void parseTestNode() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyParser parser = AlkemyParsers.typeParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestNode.class).drainTo(result);

        assertThat(result.size(), is(7));
    }

    @Test
    public void testOrdered()
    {
        final AlkemyParser parser = AlkemyParsers.typeParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestOrdered.class).drainTo(result);

        assertThat(result.size(), is(8));

        final StringBuilder sb = new StringBuilder();
        final TestOrdered to = new TestOrdered();
        result.forEach(e ->
        {
            sb.append(e.isNode() ? "" : e.get(to)).append(" ");
        });

        assertThat(" This is an example of ordered alkemyElements ", is(sb.toString()));
    }

    @Test
    public void testDeepLeaves()
    {
        final AlkemyParser parser = AlkemyParsers.typeParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestDeepLeaves.class).drainTo(result);

        assertThat(result.size(), is(7));
    }
}
