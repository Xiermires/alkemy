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
import org.alkemy.util.Assertions;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor;
import org.alkemy.visitor.impl.AlkemyElementWriter;
import org.alkemy.visitor.impl.AlkemyPreorderVisitor;

public class AlkemistBuilder
{
    public enum Mode
    {
        PREORDER_EXISTING, PREORDER_ALL, WRITE
    };

    private AlkemyLexer<AnnotatedElement> lexer = null;
    private AlkemyParser parser = null;
    private AlkemyLoadingCache cache = null;
    private AlkemyElementVisitor<?> aev = null;

    public AlkemistBuilder()
    {
    }

    // TODO: addVisitor(...)
    public AlkemistBuilder visitor(AlkemyElementVisitor<?> aev)
    {
        this.aev = aev;
        return this;
    }
    
    public Alkemist build()
    {
        Assertions.exists(aev);
        
        lexer = lexer == null ? AlkemyParsers.fieldLexer() : lexer;
        parser = parser == null ? AlkemyParsers.fieldParser(lexer) : parser;
        cache = new AlkemyLoadingCache(parser);
        return new Alkemist(cache, AlkemyPreorderVisitor.create(aev, true));
    }

    public Alkemist build(Mode mode)
    {
        Assertions.exists(mode);
        
        lexer = lexer == null ? AlkemyParsers.fieldLexer() : lexer;
        parser = parser == null ? AlkemyParsers.fieldParser(lexer) : parser;
        cache = new AlkemyLoadingCache(parser);
        return new Alkemist(cache, createAlkemyNodeVisitor(mode, aev));
    }

    public Alkemist build(AlkemyNodeVisitor anv)
    {
        Assertions.exists(anv);
        
        lexer = lexer == null ? AlkemyParsers.fieldLexer() : lexer;
        parser = parser == null ? AlkemyParsers.fieldParser(lexer) : parser;
        cache = new AlkemyLoadingCache(parser);
        return new Alkemist(cache, anv);
    }

    private AlkemyNodeVisitor createAlkemyNodeVisitor(Mode mode, AlkemyElementVisitor<?> aev)
    {
        switch (mode)
        {
            case PREORDER_EXISTING:
                return AlkemyPreorderVisitor.create(aev, false);
            case PREORDER_ALL:
                return AlkemyPreorderVisitor.create(aev, true);
            case WRITE:
                return new AlkemyElementWriter(aev);
        }
        throw new AlkemyException("Invalid mode '%s'", mode.name()); // should never happen
    }
}
