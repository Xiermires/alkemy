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

import static org.hamcrest.CoreMatchers.is;
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
import org.alkemy.util.Nodes;
import org.junit.Test;

public class TypeFieldParserTest
{
    @Test
    public void parseTestClass() throws IOException, InstantiationException, IllegalAccessException
    {
        final AlkemyElementFactory<AlkemyElement, AnnotatedElement> elementFactory = new TypeFieldAlkemyElementFactory();
        final AlkemyLexer<AlkemyElement, AnnotatedElement> lexer = TypeFieldLexer.create(elementFactory);
        final AlkemyParser<AlkemyElement> parser = TypeFieldParser.create(lexer);

        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        
        final List<AlkemyElement> result = new ArrayList<AlkemyElement>();
        Nodes.drainContentsTo(parser.parse(TestClass.class), result, x -> true);
        
        assertThat(result.size(), is(5));
        
        final TestClass tc = (TestClass) clazz.newInstance();

        //result.stream().forEach(d -> d.getValueAccessor().bindTo(tc)); TODO (agent)
        for (AlkemyElement ae : result)
        {
            ae.getValueAccessor().bindTo(tc);
        }
        assertThat(1+2+3+4+5,is(result.stream().mapToInt(d -> (int) d.getValueAccessor().get()).sum()));
    }
}
