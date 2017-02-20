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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.alkemy.ValueAccessor;
import org.alkemy.annotations.Order;
import org.alkemy.exception.InvalidArgument;
import org.alkemy.parse.impl.TestCreateInstanceParamPreserveOrder.FollowsDeclaration;
import org.alkemy.parse.impl.TestCreateInstanceParamPreserveOrder.FollowsOrder;
import org.alkemy.util.Measure;
import org.alkemy.util.ObjIntFunction;
import org.junit.BeforeClass;
import org.junit.Test;

public class AlkemizerTest
{
    private static final int ITER = 10000000; // 1e7
    static Class<?> clazz;

    @BeforeClass
    public static void pre() throws IOException, ClassNotFoundException
    {
        clazz = TestAlkemizer.class;
    }

    @Test
    public void alkemizeNodeConstructorMethod() throws IOException, NoSuchMethodException, SecurityException
    {
        final Method m = clazz.getMethod(Alkemizer.CREATE_INSTANCE, Object[].class);
        assertThat(Modifier.isStatic(m.getModifiers()), is(true));
    }
    
    @Test
    public void alkemizeOrderAnnotation()
    {
        assertTrue(clazz.isAnnotationPresent(Order.class));
        final Order order = clazz.getAnnotation(Order.class);
        assertThat(Arrays.asList(order.value()), is(Arrays.asList(new String[] { "foo", "bar" })));
    }
    
    @Test
    public void nodeConstructorArgsPreserveOrder() throws IllegalAccessException, SecurityException
    {
        final NodeConstructor ctor = MethodHandleFactory.createNodeConstructor(FollowsOrder.class);
        ctor.newInstance(1, "a");
    }
    
    @Test
    public void nodeConstructorArgsInDeclarationOrder() throws IllegalAccessException, SecurityException
    {
        final NodeConstructor ctor = MethodHandleFactory.createNodeConstructor(FollowsDeclaration.class);
        ctor.newInstance("a", 1);
    }

    @Test
    public void alkemizeIsInstrumented() throws IllegalAccessException, NoSuchMethodException, SecurityException, Throwable
    {
        final Supplier<Boolean> s = LambdaRefHelper.ref2StaticGetter(methodHandle(clazz, "is$$instrumented"), clazz,
                boolean.class);
        assertThat(s.get(), is(true));
    }

    @Test
    public void alkemizeGetterSetter() throws IOException
    {
        final Set<String> methodNames = new HashSet<>();
        for (Method m : clazz.getMethods())
        {
            methodNames.add(m.getName());
        }
        assertThat(methodNames, hasItems("get$$foo", "get$$bar", "set$$foo", "set$$bar"));
    }

    @Test(expected = InvalidArgument.class)
    public void testNodeConstructorWithArgs() throws NoSuchFieldException, SecurityException, IllegalAccessException
    {
        final NodeConstructor ctor = MethodHandleFactory.createNodeConstructor(TestAlkemizer.class);
        final TestAlkemizer tc = ctor.newInstance(1, "foo");

        assertThat(1, is(tc.foo));
        assertThat("foo", is(tc.bar));

        // throws invalid argument ex.
        ctor.newInstance(1, 2, 3);
    }

    public void testNodeConstructorNoArgs() throws NoSuchFieldException, SecurityException, IllegalAccessException
    {
        final NodeConstructor ctor = MethodHandleFactory.createNodeConstructor(TestAlkemizer.class);
        final TestAlkemizer tc = ctor.newInstance();

        assertThat(tc, is(not(nullValue())));
        assertThat(-1, is(tc.foo));
        assertThat("baz", is(tc.bar));
    }

    @Test
    public void testAccessorGetter() throws NoSuchFieldException, SecurityException, IllegalAccessException
    {
        final Field f = clazz.getDeclaredField("foo");
        final ValueAccessor accessor = MethodHandleFactory.createAccessor(f);

        final TestAlkemizer tc = new TestAlkemizer();
        assertThat(-1, is(accessor.get(tc)));
    }

    @Test
    public void testAccessorSetter() throws Throwable
    {
        final Field f = clazz.getDeclaredField("foo");
        final ValueAccessor accessor = MethodHandleFactory.createAccessor(f);

        final TestAlkemizer tc = new TestAlkemizer();
        accessor.set(1, tc);
        assertThat(1, is(accessor.get(tc)));
    }

    @Test
    public void performanceAccesingStrategies() throws Throwable
    {
        System.out.println("**** Compare accessing / creating fiels (1e7 iterations) ****");
        
        final Field field = clazz.getDeclaredField("bar");
        final Method method = clazz.getDeclaredMethod("get$$bar");
        final MethodHandle handle = methodHandle(clazz, "get$$bar");
        final Function<Object, String> function = LambdaRefHelper.ref2MemberGetter(handle, Object.class, String.class);
        final ValueAccessor accessor = MethodHandleFactory.createAccessor(clazz.getDeclaredField("bar"));
        final NodeConstructor ctor = MethodHandleFactory.createNodeConstructor(TestAlkemizer.class);
        
        final TestAlkemizer tc = new TestAlkemizer();

        // warm up
        for (int i = 0; i < ITER; i++)
        {
            @SuppressWarnings("unused")
            String bar = (String) handle.invokeExact(tc);
            bar = (String) handle.invoke(tc);
            bar = function.apply(tc);
            bar = (String) method.invoke(tc);
            bar = (String) accessor.get(tc);
        }

        System.out.println("MethodHandle#invokeExact(): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                @SuppressWarnings("unused")
                String bar = (String) handle.invokeExact(tc);
            }
        }) / 1000000 + " ms");

        System.out.println("MethodHandle#invoke(): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(handle.invoke(tc));
            }
        }) / 1000000 + " ms");

        System.out.println("LambdaMetaFactory (TestClass)String: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                function.apply(tc);
            }
        }) / 1000000 + " ms");

        System.out.println("Accessor (TestClass)String: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                accessor.get(tc);
            }
        }) / 1000000 + " ms");

        System.out.println("Reflection (get): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(field.get(tc));
            }
        }) / 1000000 + " ms");
        
        System.out.println("Reflection (call get$$bar): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(method.invoke(tc));
            }
        }) / 1000000 + " ms");
        
        final TestAlkemizerCompiledVersion tce = new TestAlkemizerCompiledVersion(-1, "baz");
        System.out.println("Direct access (getter): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                tce.get$$bar();
            }
        }) / 1000000 + " ms");

        System.out.println("create$$instance: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                ctor.newInstance(1, "two");
            }
        }) / 1000000 + " ms");
        
        System.out.println("Direct access (call static factory): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                TestAlkemizerCompiledVersion.create$$instance(1, "two");
            }
        }) / 1000000 + " ms");
        
        System.out.println("**** Compare end ****");
    }

    @Test
    public void compareLambdaRefObjectvsRefPrimitive() throws Throwable
    {
        final MethodHandle handle = methodHandle(clazz, "get$$foo");
        final Function<TestAlkemizer, Integer> f1 = LambdaRefHelper.ref2MemberGetter(handle, TestAlkemizer.class, Integer.class);
        final ObjIntFunction<TestAlkemizer> f2 = objIntLambdaRef(handle, TestAlkemizer.class);

        final TestAlkemizer tc = new TestAlkemizer();

        // warm up
        for (int i = 0; i < ITER; i++)
        {
            @SuppressWarnings("unused")
            int foo = f1.apply(tc);
            foo = f2.apply(tc);
        }

        System.out.println("LambdaMetaFactory (TestClass)Integer: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                f1.apply(tc).intValue();
            }
        }) / 1000000 + " ms");

        System.out.println("LambdaMetaFactory (TestClass)int: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                f2.apply(tc);
            }
        }) / 1000000 + " ms");
    }

    private MethodHandle methodHandle(Class<?> clazz, String methodName) throws IllegalAccessException, NoSuchMethodException,
            SecurityException
    {
        final Method method = clazz.getDeclaredMethod(methodName);
        return MethodHandles.lookup().unreflect(method);
    }

    private <T> ObjIntFunction<T> objIntLambdaRef(MethodHandle handle, Class<T> t) throws Throwable
    {
        final Method funcMethod = ObjIntFunction.class.getMethod("apply", Object.class);
        final Class<?> funcRet = funcMethod.getReturnType();
        final Class<?>[] funcParams = funcMethod.getParameterTypes();
        final MethodType funcType = MethodType.methodType(funcRet, funcParams);

        return (ObjIntFunction<T>) LambdaMetafactory
                .metafactory(MethodHandles.lookup(), funcMethod.getName(), MethodType.methodType(ObjIntFunction.class), funcType,
                        handle, handle.type()).getTarget().invoke();
    }
}
