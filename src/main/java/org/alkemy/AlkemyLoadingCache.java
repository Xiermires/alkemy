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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.agenttools.AgentTools;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.util.Node;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

class AlkemyLoadingCache
{
    // TODO: Allow external configuration
    private static final int MAXIMUM_BYTE_SIZE = 2 * 1024 * 1024;

    private final AlkemyParser parser;

    // Keep a cached version of created AlkemyTypeVisitor to enhance performance.
    private final LoadingCache<Class<?>, Node<? extends AbstractAlkemyElement<?>>> cache;

    AlkemyLoadingCache(AlkemyParser parser)
    {
        this.parser = parser;

        cache = CacheBuilder.newBuilder().maximumWeight(MAXIMUM_BYTE_SIZE)
                .weigher((k, v) -> Number.class.cast(AgentTools.getObjectSize(k)).intValue())
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(new CacheLoader<Class<?>, Node<? extends AbstractAlkemyElement<?>>>()
                {
                    @Override
                    public Node<? extends AbstractAlkemyElement<?>> load(Class<?> key) throws AlkemyException
                    {
                        return _create(key);
                    }
                });
    }

    private final Node<? extends AbstractAlkemyElement<?>> _create(Class<?> type)
    {
        return parser.parse(type);
    }

    Node<? extends AbstractAlkemyElement<?>> get(Class<?> type)
    {
        try
        {
            return cache.get(type);
        }
        catch (ExecutionException e) // TODO
        {
            throw new AlkemyException("Cache error.", e); 
        }
        catch (UncheckedExecutionException e)
        {
            throw new AlkemyException("Cache error.", e); 
        }
    }
}
