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

import java.lang.reflect.AnnotatedElement;

import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;

/**
 * Common parser implementations.
 */
public class AlkemyParsers
{
    private AlkemyParsers()
    {
    }

    public static AlkemyLexer<AnnotatedElement> fieldLexer()
    {
        final AlkemyElementFactory<AnnotatedElement> elementFactory = TypeFieldAlkemyElementFactory.create();
        return TypeFieldLexer.create(elementFactory);
    }

    /**
     * See {@link TypeParser}
     */
    public static AlkemyParser typeParser()
    {
        return TypeParser.create(fieldLexer());
    }

    public static AlkemyParser typeParser(AlkemyLexer<AnnotatedElement> lexer)
    {
        return TypeParser.create(lexer);
    }
}
