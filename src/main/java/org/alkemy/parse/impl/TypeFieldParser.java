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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.Order;
import org.alkemy.exception.InvalidOrder;
import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes;
import org.alkemy.util.TypedTable;

/**
 * The main parser. It parses fields alkemizations (see {@link AlkemyLeaf}, as well as the {@link Order} annotation.
 * <p>
 * Example:
 * 
 * <pre>
 * <code>
 * @Order("bar", "foo")
 * public class Foo 
 * {
 *  @Aggregate("weight")
 *  double foo;
 *  
 *  @Mean("height")
 *  double bar;
 * }
 * <code>
 * The parser will create a tree with two leafs such as : Root (Foo) { Leaf (bar) , Leaf (foo) }
 */
class TypeFieldParser implements AlkemyParser
{
    private final AlkemyLexer<AnnotatedElement> lexer;

    private TypeFieldParser(AlkemyLexer<AnnotatedElement> lexer)
    {
        this.lexer = lexer;
    }

    static AlkemyParser create(AlkemyLexer<AnnotatedElement> lexer)
    {
        return new TypeFieldParser(lexer);
    }

    @Override
    public Node<AbstractAlkemyElement<?>> parse(Class<?> type)
    {
        final TypedTable context = new TypedTable();
        return _parse(
                type,
                Nodes.arborescence(lexer.createNode(new AnnotatedElementWrapper(new Annotation[0]),
                        AccessorFactory.createConstructor(type), AccessorFactory.createSelfAccessor(), type, context)), context)
                .build();
    }

    private Node.Builder<AbstractAlkemyElement<?>> _parse(Class<?> type, Node.Builder<AbstractAlkemyElement<?>> parent,
            TypedTable context)
    {
        if (Object.class.equals(type))
        {
            return parent;
        }
        else
        {
            _parse(type.getSuperclass(), parent, context);
        }

        for (final Field f : sortIfRequired(type.getDeclaredFields(), type.getAnnotation(Order.class), type))
        {
            if (lexer.isLeaf(f))
            {
                parent.addChild(lexer.createLeaf(f, AccessorFactory.createAccessor(f), context));
            }
            else if (lexer.isNode(f))
            {
                _parse(f.getType(),
                        parent.addChild(lexer.createNode(f, AccessorFactory.createConstructor(f.getType()),
                                AccessorFactory.createAccessor(f), f.getType(), context)), context);
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
                    "Missing fields within the FieldOrder annotation inside the type : '%s'. It must contain all mapped fields.",
                    type.getSimpleName()); }

            final Map<String, Integer> nameOrder = new HashMap<String, Integer>();
            for (int i = 0; i < names.length; i++)
            {
                nameOrder.put(names[i], i);
            }
            Arrays.sort(
                    fields,
                    (o1, o2) ->
                    {
                        if (lexer.isLeaf(o1) || lexer.isNode(o1) && lexer.isLeaf(o2) || lexer.isNode(o2))
                        {

                            final Integer lhs = nameOrder.get(o1.getName());
                            final Integer rhs = nameOrder.get(o2.getName());

                            if (lhs == null)
                            {
                                throw new InvalidOrder("Field name '%s' not found within the type '%s'.", o1.getName(), type
                                        .getSimpleName());
                            }
                            else if (rhs == null) { throw new InvalidOrder("Field name '%s' not found within the type '%s'.", o1
                                    .getName(), type.getSimpleName()); }

                            return lhs < rhs ? -1 : rhs == lhs ? 0 : 1;
                        }
                        else if (lexer.isLeaf(o1) || lexer.isNode(o1)) // Keep non mappings to the right.
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
                    });
        }
        return fields;
    }
}
