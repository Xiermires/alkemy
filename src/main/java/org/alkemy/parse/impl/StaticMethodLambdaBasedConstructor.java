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

import java.util.Arrays;
import java.util.function.Supplier;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;
import org.alkemy.exception.InvalidArgument;

public class StaticMethodLambdaBasedConstructor implements NodeConstructor
{
    private final Class<?> type;
    private final Supplier<?> noargsCtor;
    private final NodeConstructorFunction argsCtor;

    StaticMethodLambdaBasedConstructor(Class<?> type, Supplier<?> noargsCtor, NodeConstructorFunction argsCtor)
    {
        this.type = type;
        this.noargsCtor = noargsCtor;
        this.argsCtor = argsCtor;
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return type;
    }

    @Override
    public Object newInstance(Object... args) throws AlkemyException
    {
        try
        {
            if (args.length == 0)
            {
                return noargsCtor.get();
            }
            else
            {
                return argsCtor.newInstance(args);
            }
        }
        catch (Throwable e) 
        {
            throw new AccessException("Provided arguments '%s' do not match the ctor expected arguments of type '%s'.", e, Arrays.asList(args), type);
        }
    }
}
