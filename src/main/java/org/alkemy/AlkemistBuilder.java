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

import java.lang.reflect.AnnotatedElement;

import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AlkemyParsers;

public class AlkemistBuilder
{
    private AlkemyLexer<AlkemyElement, AnnotatedElement> lexer = null;
    private AlkemyParser<AlkemyElement> parser = null;
    private AlkemyLoadingCache cache = null;
    
    public AlkemistBuilder()
    {   
    }
    
    public Alkemist build()
    {
        lexer = lexer == null ? AlkemyParsers.fieldLexer() : lexer;
        parser = parser == null ? AlkemyParsers.fieldParser(lexer) : parser;
        cache = new AlkemyLoadingCache(parser);
        return new Alkemist(cache);
    }
}
