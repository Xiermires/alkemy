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
package org.alkemy.example;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.Alkemist;
import org.alkemy.AlkemistBuilder;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.util.Conditions;
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.junit.Test;

// Injecting random generated values
public class RandomGenerator
{
    @Test
    public void generateRandoms()
    {
        final Alkemist alkemist = new AlkemistBuilder().visitor(new XorRandomGenerator()).build();
        final TestClass tc = new TestClass();

        alkemist.process(tc);
        
        assertThat(tc.i, is(both(greaterThan(5)).and(lessThan(10)).or(equalTo(5)).or(equalTo(10)))); 
        assertThat(tc.d, is(both(greaterThan(9.25)).and(lessThan(11.5)).or(equalTo(9.25)).or(equalTo(11.5))));
    }
    
    // The visitor that works on the AlkemyElements. 
    static class XorRandomGenerator implements AlkemyElementVisitor<RandomElement, Object>
    {
        @Override
        public void visit(RandomElement e, Reference<Object> parent, Object... params)
        {
            e.set(nextDouble(e.min, e.max), parent.get()); // generates and sets the next random
        }

        @Override
        public RandomElement map(AlkemyElement e)
        {
            return new RandomElement(e);
        }

        @Override
        public boolean accepts(Class<?> type)
        {
            return XorRandomGenerator.class.equals(type);
        }
        
        protected double nextDouble(double min, double max)
        {
            return min + (nextDouble() * ((max - min)));
        }
        
        private long seed = System.nanoTime();
        
        /**
         * Return an uniformly distributed double number between (0-1).
         * <p>
         * Zero is not inclusive since we are using Xorshift.
         * <p>
         * One is excluded as well via the +1 in Long.MAX_VALUE + 1.
         */
        double nextDouble()
        {
            double d = xorshift64() / (double) (Long.MAX_VALUE + 1);
            return d < 0 ? -d : d; // xorshift64() generates values in the whole Long.MIN_VALUE to Long.MAX_VALUE. Ensure we
                                   // return positive numbers (0-1).
        }

        /**
         * Xorshift implementation 2^64 version.
         * <p>
         * Shifting triplet values selected from G. Marsaglia '<a href="https://www.jstatsoft.org/article/view/v008i14">Xorshift
         * RNGs</a>'.</a>'
         */
        long xorshift64()
        {
            seed ^= seed << 13;
            seed ^= seed >>> 7;
            seed ^= seed << 17;
            return seed;
        }
    }

    // The custom AlkemyElement built out of the Random marker.
    static class RandomElement extends AbstractAlkemyElement<RandomElement>
    {
        double min, max;

        protected RandomElement(AbstractAlkemyElement<?> other)
        {
            super(other);

            final Random a = other.desc().getAnnotation(Random.class);
            Conditions.requireNonNull(a); 

            min = a.min();
            max = a.max();
        }
    }

    // The visitor marker
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf(XorRandomGenerator.class)
    @interface Random
    {
        double min();

        double max();
    }
}
