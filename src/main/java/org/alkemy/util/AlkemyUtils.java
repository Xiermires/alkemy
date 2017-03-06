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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.alkemy.parse.impl.AbstractAlkemyElement;
import org.alkemy.parse.impl.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.visitor.AlkemyElementVisitor;

public class AlkemyUtils
{
    public static Object getNodeInstance(Node<? extends AbstractAlkemyElement<?>> e, Object parent, boolean createIfNull)
    {
        Object node = e.data().get(parent);
        if (createIfNull && node == null)
        {
            node = e.data().newInstance();
            e.data().set(node, parent);
        }
        return node;
    }

    /**
     * Use this method if you are to process the same flat node (node with no grand children)
     * several times and you want to enhance the processing performance.
     * <p>
     * This method is meant to be combined with:
     * <ul>
     * <li>{@link #createFlatNodeInstance(AbstractAlkemyElement[], AlkemyElementVisitor, Node, Class)}
     * <li>{@link #processFlatNodeInstance(List, AlkemyElementVisitor, Node, Object)}
     * </ul>
     */
    public static <R, P, E extends AbstractAlkemyElement<E>> List<E> mapFlatNodeLeafs(AlkemyElementVisitor<P, E> aev,
            Node<? extends AbstractAlkemyElement<?>> e, Class<R> retType, boolean proceedIfNotSupported)
    {
        Assertions.noneNull(e, retType);
        Assertions.isTrue(e.branchDepth() == 1, "Provided AlkemyElement node '%s' is not flat", e.data().toString());

        boolean supported = true;
        final List<E> mapped = new ArrayList<E>();
        for (Node<? extends AbstractAlkemyElement<?>> node : e.children())
        {
            final E map = aev.map(new AlkemyElement(node.data()));
            if (map != null)
            {
                mapped.add(map);
            }
            else if (aev.accepts(node.data().alkemyType()))
            {
                mapped.add(null);
            }
            else supported = false;
        }
        return supported || proceedIfNotSupported ? mapped : null;
    }

    /**
     * Creates an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)}
     */
    public static <R, P, E extends AbstractAlkemyElement<E>> R createFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends AbstractAlkemyElement<?>> e, Class<R> retType)
    {
        final Object[] args = new Object[mapped.size()];
        for (int i = 0; i < args.length; i++)
        {
            args[i] = aev.create(mapped.get(i));
        }
        return e.data().safeNewInstance(retType, args);
    }

    /**
     * Creates an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)} and includes a
     * parameter.
     */
    public static <R, P, E extends AbstractAlkemyElement<E>> R createFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends AbstractAlkemyElement<?>> e, Class<R> retType, P parameter)
    {
        final Object[] args = new Object[mapped.size()];
        for (int i = 0; i < args.length; i++)
        {
            args[i] = aev.create(mapped.get(i), parameter);
        }
        return e.data().safeNewInstance(retType, args);
    }

    /**
     * Process an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)}
     */
    public static <R, P, E extends AbstractAlkemyElement<E>> R processFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends AbstractAlkemyElement<?>> e, R instance)
    {
        for (E map : mapped)
        {
            aev.visit(map, instance);
        }
        return instance;
    }

    /**
     * Process an instance of a flat node (node with no grand children). This method is meant to be
     * called after a flat node has been processed with
     * {@link #mapFlatNodeLeafs(AlkemyElementVisitor, Node, Class, boolean)} and includes a
     * parameter.
     */
    public static <R, P, E extends AbstractAlkemyElement<E>> R processFlatNodeInstance(List<E> mapped,
            AlkemyElementVisitor<P, E> aev, Node<? extends AbstractAlkemyElement<?>> e, R instance, P parameter)
    {
        for (E map : mapped)
        {
            aev.visit(map, instance, parameter);
        }
        return instance;
    }

    public static void setEnum(Class<?> type, BiConsumer<Object, Object> setter, Object value, Object parent)
    {
        setter.accept(parent, toEnum(type, value));
    }

    public static void setEnum(Field f, Object value, Object parent) throws IllegalArgumentException, IllegalAccessException
    {
        f.set(parent, toEnum(f.getType(), value));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    // caller responsibility
    public static Object toEnum(Class<?> type, Object value)
    {
        if (value instanceof String)
        {
            return Enum.valueOf((Class<? extends Enum>) type, (String) value);
        }
        else return value;
    }
}
