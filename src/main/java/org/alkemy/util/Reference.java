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
package org.alkemy.util;

public interface Reference<T>
{
    public enum Type { IN, INOUT };
    
    T get();

    void set(T t);
    
    Type type();

    public static <T> Reference<T> inOut(T t)
    {
        return new RefInOut<T>(t);
    }
    
    public static <T> Reference<T> inOut()
    {
        return new RefInOut<T>(null);
    }
    
    public static <T> Reference<T> in(T t)
    {
        return new RefIn<T>(t);
    }
    
    static class RefIn<T> implements Reference<T>
    {
        private T t;

        private RefIn(T t)
        {
            this.t = t;
        }

        @Override
        public T get()
        {
            return t;
        }

        @Override
        public void set(T t)
        {
            throw new UnsupportedOperationException("In reference (read only).");
        }

        @Override
        public Type type()
        {
            return Type.IN;
        }
    }

    static class RefInOut<T> implements Reference<T>
    {
        private T t;

        private RefInOut(T t)
        {
            this.t = t;
        }

        @Override
        public T get()
        {
            return t;
        }

        @Override
        public void set(T t)
        {
            this.t = t;
        }

        @Override
        public Reference.Type type()
        {
            return Type.INOUT;
        }
    }
}
