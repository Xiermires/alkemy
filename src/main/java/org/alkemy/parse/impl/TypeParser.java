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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
 * The main parser. It parses fields and method alkemizations (see {@link AlkemyLeaf}, as well as
 * the {@link Order} annotation.
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
 *  
 *  @Schedule(every = 5000)
 *  @Using({"foo"},{"bar"})
 *  void method(double foo, double bar) { ... }
 *  
 * }
 * <code>
 * The parser will create a tree such as follows: Root (Foo { method }) { Leaf (bar) , Leaf (foo) }
 */
class TypeParser implements AlkemyParser
{
    private final AlkemyLexer<AnnotatedMember, AnnotatedMember> lexer;

    private TypeParser(AlkemyLexer<AnnotatedMember, AnnotatedMember> lexer)
    {
        this.lexer = lexer;
    }

    static AlkemyParser create(AlkemyLexer<AnnotatedMember, AnnotatedMember> lexer)
    {
        return new TypeParser(lexer);
    }

    @Override
    public Node<AbstractAlkemyElement<?>> parse(Class<?> type)
    {
        final TypedTable context = new TypedTable();

        final AnnotatedMember am = new AnnotatedMember(type.getName(), type, type, type);

        return _parse(
                am,
                Nodes.arborescence(lexer.createNode(new AnnotatedMember(type.getName(), type, type, type), AccessorFactory
                        .createConstructor(type, type), AccessorFactory.createSelfAccessor(type), AccessorFactory
                        .createInvokers(getLeafMethods(type)), type, context)), context).build();
    }

    private Node.Builder<AbstractAlkemyElement<?>> _parse(AnnotatedMember type, Node.Builder<AbstractAlkemyElement<?>> parent,
            TypedTable context)
    {
        for (final Field f : sortIfRequired(type.getComponentType().getDeclaredFields(), type.getComponentType().getAnnotation(
                Order.class), type.getComponentType()))
        {
            final AnnotatedMember am = new AnnotatedMember(f.getName(), f, f.getType(), f.getDeclaringClass(), getGenericTypes(f));

            if (lexer.isLeaf(am))
            {
                parent.addChild(lexer.createLeaf(am, AccessorFactory.createAccessor(f), context));
            }
            else if (lexer.isNode(am))
            {
                _parse(am, parent.addChild(lexer.createNode(am, AccessorFactory.createConstructor(am.getType(), am.getComponentType()), AccessorFactory
                        .createAccessor(f), AccessorFactory.createInvokers(getLeafMethods(am.getComponentType())), am.getType(),
                        context)), context);
            }
        }
        return parent;
    }

    private Type[] getGenericTypes(Field f)
    {
        final Type[] genericTypes;
        final Type genericType = f.getGenericType();
        if (genericType instanceof ParameterizedType)
        {
            genericTypes = ((ParameterizedType) genericType).getActualTypeArguments();
        }
        else
        {
            genericTypes = new Type[] { genericType };
        }
        return genericTypes;
    }

    private List<Method> getLeafMethods(Class<?> type)
    {
        final List<Method> ms = new ArrayList<Method>();
        for (Method m : type.getDeclaredMethods())
        {
            if (lexer.isLeaf(new AnnotatedMember(m.getName(), m, null, m.getDeclaringClass())))
            {
                ms.add(m);
            }
        }
        return ms;
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
            Arrays.sort(fields, (o1, o2) ->
            {
                final AnnotatedMember am1 = new AnnotatedMember(o1.getName(), o1, o1.getType(), o1.getDeclaringClass(),
                        getGenericTypes(o1));
                final AnnotatedMember am2 = new AnnotatedMember(o2.getName(), o2, o2.getType(), o2.getDeclaringClass(),
                        getGenericTypes(o2));
                if (lexer.isLeaf(am1) || lexer.isNode(am1) && lexer.isLeaf(am2) || lexer.isNode(am2))
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
                // keep non mappings to the right.
                    else if (lexer.isLeaf(am1) || lexer.isNode(am1))
                    {
                        return 1;
                    }
                    else if (lexer.isLeaf(am2) || lexer.isNode(am2))
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
