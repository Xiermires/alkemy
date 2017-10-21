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
package org.alkemy.parse.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.Order;
import org.alkemy.exception.InvalidOrder;
import org.alkemy.parse.AlkemyLexer;
import org.alkemy.parse.AlkemyParser;
import org.alkemy.parse.MethodInvoker;
import org.alkemy.parse.NodeFactory;
import org.alkemy.parse.ValueAccessor;
import org.alkemy.util.Node;
import org.alkemy.util.Nodes;
import org.alkemy.util.Types;

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
public class TypeParser implements AlkemyParser
{
    private final AlkemyLexer<AnnotatedMember> lexer;

    private TypeParser(AlkemyLexer<AnnotatedMember> lexer)
    {
        this.lexer = lexer;
    }

    public static AlkemyParser create(AlkemyLexer<AnnotatedMember> lexer)
    {
        return new TypeParser(lexer);
    }

    @Override
    public Node<AlkemyElement> parse(Class<?> type)
    {
        final ValueAccessor valueAccessor = AccessorFactory.createSelfAccessor(type);
        final NodeFactory nodeFactory = AccessorFactory.createNodeFactory(valueAccessor);
        final List<MethodInvoker> methodInvokers = AccessorFactory.createInvokers(getLeafMethods(type));
        final AnnotatedMember am = new AnnotatedMember(type, type.getName(), type);
        final AlkemyElement root = lexer.createNode(am, nodeFactory, valueAccessor, methodInvokers, type);
        return _parse(nodeFactory, Nodes.arborescence(root)).build();
    }

    private Node.Builder<AlkemyElement> _parse(NodeFactory parentNodeFactory, Node.Builder<AlkemyElement> parent)
    {
        final Class<?> parentComponentType = parentNodeFactory.componentType();
        final Class<?> parentType = parentComponentType != null ? parentComponentType : parentNodeFactory.type();

        for (final Field f : sortIfRequired(parentType.getDeclaredFields(), parentType.getAnnotation(Order.class), parentType))
        {
            final Class<?> childComponentType = Types.getComponentType(f);
            final Class<?> childType = childComponentType != null ? childComponentType : f.getType();

            final AnnotatedMember am = new AnnotatedMember(f, f.getName(), childType);
            if (lexer.isLeaf(am))
            {
                final ValueAccessor valueAccessor = AccessorFactory.createValueAccessor(f);
                parent.addChild(lexer.createLeaf(am, valueAccessor));
            }
            else if (lexer.isNode(am))
            {
                final ValueAccessor valueAccessor = AccessorFactory.createValueAccessor(f);
                final NodeFactory nodeFactory = AccessorFactory.createNodeFactory(valueAccessor);
                final List<Method> leafMethods = getLeafMethods(parentType);
                final List<MethodInvoker> methodInvokers = AccessorFactory.createInvokers(leafMethods);
                final AlkemyElement node = lexer.createNode(am, nodeFactory, valueAccessor, methodInvokers, am.type);
                _parse(nodeFactory, parent.addChild(node));
            }
        }
        return parent;
    }

    private List<Method> getLeafMethods(Class<?> type)
    {
        final List<Method> ms = new ArrayList<Method>();
        for (Method m : type.getDeclaredMethods())
        {
            if (lexer.isLeaf(new AnnotatedMember(m, m.getName(), null)))
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
            final Set<String> fieldNames = Arrays.asList(fields).stream().map(f -> f.getName()).collect(Collectors.toSet());
            final Map<String, Integer> fieldOrder = new HashMap<>();
            final String[] orderedFieldNames = order.value();
            for (int i = 0; i < orderedFieldNames.length; i++)
                fieldOrder.put(orderedFieldNames[i], i);

            if (!fieldNames.containsAll(fieldOrder.keySet()))
            { //
                throw new InvalidOrder("Defined order contains fields not present in '{}'.", type.getName());
            }

            Arrays.sort(fields, (f1, f2) ->
            {
                if (fieldOrder.containsKey(f1.getName()) && fieldOrder.containsKey(f2.getName()))
                {
                    final Integer lhs = fieldOrder.get(f1.getName());
                    final Integer rhs = fieldOrder.get(f2.getName());

                    return lhs < rhs ? -1 : rhs == lhs ? 0 : 1;
                }
                else if (fieldOrder.containsKey(f1.getName()))
                {
                    return 1;
                }
                else if (fieldOrder.containsKey(f2.getName()))
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
