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

import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AlkemyParsers;
import org.alkemy.util.Conditions;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor;
import org.alkemy.visitor.impl.AlkemyElementReader;
import org.alkemy.visitor.impl.AlkemyElementWriter;

public class AlkemistBuilder
{
    public enum Mode
    {
        READ_EXISTING, READ_ALL, WRITE
    };

    private AlkemyLexer<AnnotatedElement> lexer = null;
    private AlkemyParser parser = null;
    private AlkemyLoadingCache cache = null;
    private AlkemyElementVisitor<?, Object> aev = null;
    private boolean parallel = false;

    public AlkemistBuilder()
    {
    }

    // TODO: addVisitor(...)
    public AlkemistBuilder visitor(AlkemyElementVisitor<?, Object> aev)
    {
        this.aev = aev;
        return this;
    }

    public Alkemist build()
    {
        Conditions.requireNonNull(aev);
        
        lexer = lexer == null ? AlkemyParsers.fieldLexer() : lexer;
        parser = parser == null ? AlkemyParsers.fieldParser(lexer) : parser;
        cache = new AlkemyLoadingCache(parser);
        return new Alkemist(cache, new AlkemyElementReader(aev, parallel, true));
    }

    public Alkemist build(Mode mode)
    {
        Conditions.requireNonNull(mode);
        
        lexer = lexer == null ? AlkemyParsers.fieldLexer() : lexer;
        parser = parser == null ? AlkemyParsers.fieldParser(lexer) : parser;
        cache = new AlkemyLoadingCache(parser);
        return new Alkemist(cache, createAlkemyNodeVisitor(mode, aev));
    }

    public Alkemist build(AlkemyNodeVisitor anv)
    {
        Conditions.requireNonNull(anv);
        
        lexer = lexer == null ? AlkemyParsers.fieldLexer() : lexer;
        parser = parser == null ? AlkemyParsers.fieldParser(lexer) : parser;
        cache = new AlkemyLoadingCache(parser);
        return new Alkemist(cache, anv);
    }

    private AlkemyNodeVisitor createAlkemyNodeVisitor(Mode mode, AlkemyElementVisitor<?, Object> aev)
    {
        switch (mode)
        {
            case READ_EXISTING:
                return new AlkemyElementReader(aev, parallel, false);
            case READ_ALL:
                return new AlkemyElementReader(aev, parallel, false);
            case WRITE:
                return new AlkemyElementWriter(aev, aev, parallel);
        }
        throw new AlkemyException("Invalid mode '%s'", mode.name()); // should never happen
    }
}
