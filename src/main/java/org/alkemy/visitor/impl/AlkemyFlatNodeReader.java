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

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.util.Assertions;
import org.alkemy.util.Nodes.TypifiedNode;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.FixedAlkemyTypeReader;

/**
 * A fast reader implementation for flat nodes (nodes with children, but no grand children) defining
 * a single alkemy type.
 * <p>
 * This class doesn't do any {@link AlkemyElementVisitor#accepts(Class)}, it is responsibility of
 * the client of this class to ensure that the defined type E and the provided
 * {@link AlkemyElementVisitor}s correlate.
 * <p>
 * This implementation performs between [4x-7x] faster than the {@link AlkemyPreorderReader} one.
 */
public class AlkemyFlatNodeReader<R, P, E extends AbstractAlkemyElement<E>> implements FixedAlkemyTypeReader<R, P, E>
{
    private final TypifiedNode<R, ? extends AbstractAlkemyElement<?>> root;
    private final E[] leafs;
    private final Object[] args;

    @SuppressWarnings("unchecked")
    public AlkemyFlatNodeReader(TypifiedNode<R, ? extends AbstractAlkemyElement<?>> node, Function<AlkemyElement, E> factory)
    {
        Assertions.noneNull(node, factory);
        Assertions.isTrue(node.branchDepth() == 1,
                "The node of type : '%s' is not a flat node (has children, hasn't grandchildren", node.data().targetName());

        final List<E> list = node.children().stream().map(e -> factory.apply(new AlkemyElement(e.data())))
                .collect(Collectors.toList());

        final Class<E> type = (Class<E>) list.get(0).getClass();
        final E[] array = (E[]) Array.newInstance(type, list.size());

        for (int i = 0; i < list.size(); i++)
        {
            array[i] = list.get(i);
        }
        this.root = node;
        this.leafs = array;
        this.args = new Object[array.length];
    }

    @Override
    public R create(AlkemyElementVisitor<?, E> aev)
    {
        for (int i = 0; i < leafs.length; i++)
        {
            args[i] = aev.create(leafs[i]);
        }
        return root.data().newInstance(root.type(), args);
    }

    @Override
    public R create(AlkemyElementVisitor<P, E> aev, P parameter)
    {
        for (int i = 0; i < leafs.length; i++)
        {
            args[i] = aev.create(leafs[i], parameter);
        }
        return root.data().newInstance(root.type(), args);
    }

    @Override
    public R accept(AlkemyElementVisitor<?, E> aev, R parameter)
    {
        for (int i = 0; i < leafs.length; i++)
        {
            aev.visit(leafs[i], parameter);
        }
        return parameter;
    }

    @Override
    public R accept(AlkemyElementVisitor<P, E> aev, R param1, P param2)
    {
        for (int i = 0; i < leafs.length; i++)
        {
            aev.visit(leafs[i], param1, param2);
        }
        return param1;
    }
}
