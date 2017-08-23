/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.alkemy;

import java.util.concurrent.TimeUnit;

import org.agenttools.Agents;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.impl.AlkemyElement;
import org.alkemy.util.Assertions;
import org.alkemy.util.Node;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class NodeCache
{
    private final AlkemyParser parser;
    private final LoadingCache<Class<?>, Node<AlkemyElement>> cache;

    public NodeCache(AlkemyParser parser, long maxSize)
    {
        this.parser = parser;
        this.cache = CacheBuilder.newBuilder()//
                .maximumWeight(maxSize)//
                .weigher((k, v) -> (int) Agents.getObjectSize(k))//
                .expireAfterAccess(60, TimeUnit.MINUTES)//
                .build(new CacheLoader<Class<?>, Node<AlkemyElement>>()
                {
                    @Override
                    public Node<AlkemyElement> load(Class<?> key) throws AlkemyException
                    {
                        return _create(key);
                    }
                });
    }

    private final Node<AlkemyElement> _create(Class<?> type)
    {
        return parser.parse(type);
    }

    public Node<AlkemyElement> get(Class<?> type)
    {
        Assertions.nonNull(type);
        try
        {
            return cache.get(type);
        }
        catch (Exception e)
        {
            throw new AlkemyException("Can't create an alkemy tree for The requested type '%s'.", e, type.getName());
        }
    }
}
