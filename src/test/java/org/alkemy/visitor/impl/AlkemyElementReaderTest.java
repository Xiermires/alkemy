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
package org.alkemy.visitor.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Stack;

import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.Alkemist;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.junit.Test;

public class AlkemyElementReaderTest
{
    @Test
    public void testAlkemyElementReader()
    {
        final ObjectReader or = new ObjectReader(new Stack<Integer>());
        final AlkemyElementReader aer = new AlkemyElementReader(or, false);
        Alkemist.process(new TestReader(), aer);
        
        assertThat(or.stack.size(), is(8));
    }
    
    // Implements both supplier & consumer
    static class ObjectReader implements AlkemyElementVisitor<AlkemyElement, Object>
    {
        private Stack<Integer> stack;

        ObjectReader(Stack<Integer> stack)
        {
            this.stack = stack;
        }

        @Override
        public void visit(Reference<Object> ref, AlkemyElement e)
        {
            stack.push(Integer.valueOf((int) e.get(ref.get())));
        }

        @Override
        public AlkemyElement map(AlkemyElement e)
        {
            return e;
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return ObjectReader.class.equals(type);
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD })
        @AlkemyLeaf(ObjectReader.class)
        public @interface Bar
        {
        }
    }
}
