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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.alkemy.annotations.Order;
import org.alkemy.core.AccessorFactory;
import org.alkemy.core.AlkemyElement;
import org.alkemy.core.ValueAccessor;
import org.alkemy.exception.InvalidOrder;
import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.util.Node;

public class TypeFieldParser<E extends AlkemyElement> implements AlkemyParser<E>
{
    private final AlkemyLexer<E, AnnotatedElement> lexer;

    private TypeFieldParser(AlkemyLexer<E, AnnotatedElement> lexer)
    {
        this.lexer = lexer;
    }

    public static <E extends AlkemyElement> AlkemyParser<E> create(AlkemyLexer<E, AnnotatedElement> lexer)
    {
        return new TypeFieldParser<E>(lexer);
    }

    @Override
    public Node<E> parse(Class<?> type)
    {
        final Node<E> root = new Node<E>(lexer.createNode(new AnnotatedElementWrapper(new Annotation[0]), null, type), null, new ArrayList<Node<E>>());
        return _parse(type, root);
    }

    private Node<E> _parse(Class<?> type, Node<E> parent)
    {
        if (Object.class.equals(type))
        {
            return parent;
        }
        else
        {
            _parse(type.getSuperclass(), parent);
        }

        for (final Field f : sortIfRequired(type.getDeclaredFields(), type.getAnnotation(Order.class), type))
        {
            if (lexer.isLeaf(f))
            {
                final ValueAccessor valueAccessor = AccessorFactory.createAccessor(f);
                parent.children().add(new Node<E>(lexer.createLeaf(f, valueAccessor), parent, Collections.<Node<E>> emptyList()));
            }
            else if (lexer.isNode(f))
            {
                final Node<E> tmp = new Node<E>(lexer.createNode(new AnnotatedElementWrapper(new Annotation[0]), null, type), null, new ArrayList<Node<E>>());
                final ValueAccessor valueAccessor = AccessorFactory.createAccessor(f);
                parent.children().add(new Node<E>(lexer.createNode(f, valueAccessor, f.getType()), parent, _parse(f.getType(), tmp).children()));
            }
        }
        return parent;
    }

    private Field[] sortIfRequired(Field[] fields, Order order, final Class<?> type)
    {
        if (order != null)
        {
            final String[] names = order.value();
            if (names.length != fields.length) { throw new InvalidOrder(
                    "Missing fields within the FieldOrder annotation inside the type : '%s'. It must contain all mapped fields.", type.getSimpleName()); }

            final Map<String, Integer> nameOrder = new HashMap<String, Integer>();
            for (int i = 0; i < names.length; i++)
            {
                nameOrder.put(names[i], i);
            }
            Arrays.sort(fields, new Comparator<Field>()
            {
                @Override
                public int compare(Field o1, Field o2)
                {
                    if (lexer.isLeaf(o1) || lexer.isNode(o1) && lexer.isLeaf(o2) || lexer.isNode(o2))
                    {

                        final Integer lhs = nameOrder.get(o1.getName());
                        final Integer rhs = nameOrder.get(o2.getName());

                        if (lhs == null)
                        {
                            throw new InvalidOrder("Field name '%s' not found within the type '%s'.", o1.getName(), type.getSimpleName());
                        }
                        else if (rhs == null) { throw new InvalidOrder("Field name '%s' not found within the type '%s'.", o1.getName(), type.getSimpleName()); }

                        return lhs < rhs ? -1 : rhs == lhs ? 0 : 1;
                    }
                    // Keep non mappings to the right.
                    else if (lexer.isLeaf(o1) || lexer.isNode(o1))
                    {
                        return 1;
                    }
                    else if (lexer.isLeaf(o2) || lexer.isNode(o2))
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            });
        }
        return fields;
    }
}
