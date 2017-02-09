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
package org.alkemy.parser.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import org.alkemy.alkemizer.AlkemizerTest;
import org.alkemy.core.AlkemyElement;
import org.alkemy.core.AlkemyElementFactory;
import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.TypeFieldAlkemyElementFactory;
import org.alkemy.parse.impl.TypeFieldLexer;
import org.alkemy.parse.impl.TypeFieldParser;
import org.alkemy.util.Node;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TypeFieldParserTest
{
    static Class<?> clazz;
    
    @BeforeClass
    public static void pre() throws IOException
    {
        clazz = AlkemizerTest.alkemize("org.alkemy.parser.impl.TestClass");
    }
    
    @Test
    public void parseTestClass() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyElementFactory<AlkemyElement, AnnotatedElement> elementFactory = new TypeFieldAlkemyElementFactory();
        final AlkemyLexer<AlkemyElement, AnnotatedElement> lexer = TypeFieldLexer.create(elementFactory);
        final AlkemyParser<AlkemyElement> parser = TypeFieldParser.create(lexer);
        
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestClass.class).drainTo(result);
        
        assertThat(result.size(), is(5));
        
        final TestClass tc = (TestClass) clazz.newInstance();
        for (AlkemyElement e : result)
        {
            e.getValueAccessor().bindTo(tc);
        }
        assertThat(1 + 2 + 3 + 4 + 5, is(result.stream().mapToInt(d -> (int) d.getValueAccessor().get()).sum()));
    }

    @Test
    public void parseTestNode() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyElementFactory<AlkemyElement, AnnotatedElement> elementFactory = new TypeFieldAlkemyElementFactory();
        final AlkemyLexer<AlkemyElement, AnnotatedElement> lexer = TypeFieldLexer.create(elementFactory);
        final AlkemyParser<AlkemyElement> parser = TypeFieldParser.create(lexer);
        
        AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestNode");
        
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestNode.class).drainTo(result);
        
        assertThat(result.size(), is(6));
    }
    
    @Test
    public void testOrdered() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyElementFactory<AlkemyElement, AnnotatedElement> elementFactory = new TypeFieldAlkemyElementFactory();
        final AlkemyLexer<AlkemyElement, AnnotatedElement> lexer = TypeFieldLexer.create(elementFactory);
        final AlkemyParser<AlkemyElement> parser = TypeFieldParser.create(lexer);

        final Class<?> orderedClass = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestOrdered");

        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        parser.parse(TestOrdered.class).drainTo(result);
        
        assertThat(result.size(), is(7));

        final StringBuilder sb = new StringBuilder();
        final TestOrdered to = (TestOrdered) orderedClass.newInstance();
        for (AlkemyElement e : result)
        {
            e.getValueAccessor().bindTo(to);
            sb.append(e.getValueAccessor().get()).append(" ");
        }
        assertThat("This is an example of ordered alkemyElements ", is(sb.toString()));
    }

    @Test
    @Ignore
    // According to the spec Class#getFile, this test can succeed or fail.
    public void testUnordered() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyElementFactory<AlkemyElement, AnnotatedElement> elementFactory = new TypeFieldAlkemyElementFactory();
        final AlkemyLexer<AlkemyElement, AnnotatedElement> lexer = TypeFieldLexer.create(elementFactory);
        final AlkemyParser<AlkemyElement> parser = TypeFieldParser.create(lexer);

        final Class<?> unorderedClass = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestUnordered");

        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        final Node<AlkemyElement> parsedElements = parser.parse(TestUnordered.class);

        parsedElements.drainTo(result);
        
        assertThat(result.size(), is(5));

        StringBuilder sb = new StringBuilder();
        final TestUnordered tu = (TestUnordered) unorderedClass.newInstance();
        for (AlkemyElement e : result)
        {
            e.getValueAccessor().bindTo(tu);
            sb.append(e.getValueAccessor().get()).append(" ");
        }
        assertThat("Hello 0 World 1 true ", is(not(sb.toString())));
    }

}
