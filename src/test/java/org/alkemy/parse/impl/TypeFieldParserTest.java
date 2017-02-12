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
package org.alkemy.parse.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alkemy.core.AlkemyElement;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.util.Node;
import org.junit.Ignore;
import org.junit.Test;

public class TypeFieldParserTest
{
    @Test
    public void parseTestClass() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyParser<AlkemyElement> parser = AlkemyParsers.defaultParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestClass.class).drainTo(result);
        
        assertThat(result.size(), is(5));

        final TestClass tc = new TestClass();
        assertThat(1 + 2 + 3 + 4 + 5, is(result.stream().mapToInt(d -> (int) d.get(tc)).sum()));
    }

    @Test
    public void parseTestNode() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyParser<AlkemyElement> parser = AlkemyParsers.defaultParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestNode.class).drainTo(result);

        assertThat(result.size(), is(6));
    }

    @Test
    public void testOrdered() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyParser<AlkemyElement> parser = AlkemyParsers.defaultParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestOrdered.class).drainTo(result);

        assertThat(result.size(), is(7));

        final StringBuilder sb = new StringBuilder();
        final TestOrdered to = new TestOrdered();
        result.forEach(e ->
        {
            sb.append(e.get(to)).append(" ");
        });

        assertThat("This is an example of ordered alkemyElements ", is(sb.toString()));
    }

    @Test
    @Ignore
    // According to the spec Class#getFile, this test can succeed or fail.
    public void testUnordered() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyParser<AlkemyElement> parser = AlkemyParsers.defaultParser();
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        final Node<AlkemyElement> parsedElements = parser.parse(TestUnordered.class);

        parsedElements.drainTo(result);

        assertThat(result.size(), is(5));

        StringBuilder sb = new StringBuilder();
        final TestUnordered tu = new TestUnordered();
        result.forEach(e ->
        {
            sb.append(e.get(tu)).append(" ");
        });
        assertThat("Hello 0 World 1 true ", is(not(sb.toString())));
    }

}
