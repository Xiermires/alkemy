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

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.util.Node;
import org.alkemy.util.Reference;
import org.alkemy.visitor.AlkemyElementVisitor;
import org.alkemy.visitor.AlkemyNodeVisitor;

public class AlkemyElementWriter implements AlkemyNodeVisitor
{
    private final static Object NOT_ACCEPTED = new Object();
    private AlkemyElementVisitor<?, Object> supplier;
    private AlkemyElementVisitor<?, Object> consumer;
    private boolean parallel;

    public AlkemyElementWriter(AlkemyElementVisitor<?, Object> supplier, AlkemyElementVisitor<?, Object> consumer,
            boolean parallel)
    {
        this.supplier = supplier;
        this.consumer = consumer;
        this.parallel = parallel;
    }

    @Override
    public void visit(Node<? extends AbstractAlkemyElement<?>> root, Reference<Object> rootObj)
    {
        final Object[] params = new Object[root.children().size()];
        stream(root, root.children()).forEach(processNode(rootObj.get(), params, new Increment()));

        if (validateParameters(params))
        {
            final Reference<Object> ref = Reference.inOut();
            root.data().accept(consumer, ref, params);
            rootObj.set(ref.get());
        }
    }

    private Consumer<? super Node<? extends AbstractAlkemyElement<?>>> processNode(Object parent, Object[] params,
            IntSupplier incr)
    {
        return e ->
        {
            if (e.hasChildren())
            {
                final Object[] childParams = new Object[e.children().size()];
                stream(e, e.children()).forEach(processNode(e.data().get(parent), childParams, new Increment()));
                final Reference<Object> ref = Reference.inOut();
                if (validateParameters(childParams))
                {
                    e.data().accept(consumer, ref, childParams);
                    params[incr.getAsInt()] = ref.get();
                }
                else
                {
                    params[incr.getAsInt()] = null; // leave node as null
                }
            }
            else
            {
                if (supplier.accepts(e.data().visitorType()))
                {
                    final Reference<Object> ref = Reference.inOut();
                    e.data().accept(supplier, ref);
                    params[incr.getAsInt()] = ref.get();
                }
                else
                {
                    params[incr.getAsInt()] = NOT_ACCEPTED;
                }
            }
        };
    }

    private boolean validateParameters(Object[] params)
    {
        for (int i = 0; i < params.length; i++)
            if (params[i] == NOT_ACCEPTED) return false;

        return true;
    }

    private <T> Stream<T> stream(Node<? extends AbstractAlkemyElement<?>> node, Collection<T> col)
    {
        return parallel && !node.data().isOrdered() ? col.parallelStream() : col.stream().sequential();
    }

    class Increment implements IntSupplier
    {
        int inc = 0;

        Increment()
        {
        }

        @Override
        public int getAsInt()
        {
            return inc++;
        }
    }
}
