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

import org.alkemy.AlkemyNodes;
import org.alkemy.annotations.Order;
import org.alkemy.exception.AlkemyException;
import org.alkemy.parse.impl.AccessorFactory.SelfAccessor;
import org.alkemy.parse.impl.TestAlkemizer.Lorem;
import org.alkemy.parse.impl.TestCreateInstanceParamPreserveOrder.FollowsDeclaration;
import org.alkemy.parse.impl.TestCreateInstanceParamPreserveOrder.FollowsOrder;
import org.alkemy.util.AlkemyUtils;
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
        final Method m = clazz.getMethod(FieldOrderWriter.CREATE_INSTANCE, Object[].class);
        assertThat(Modifier.isStatic(m.getModifiers()), is(true));
    }

    @Test
    public void alkemizeOrderAnnotation()
    {
        assertTrue(clazz.isAnnotationPresent(Order.class));
        final Order order = clazz.getAnnotation(Order.class);
        assertThat(Arrays.asList(order.value()), is(Arrays.asList(new String[] { "foo", "bar", "ipsum", "dolor" })));
    }

    @Test
    public void nodeConstructorArgsPreserveOrder() throws IllegalAccessException, SecurityException
    {
        final NodeFactory ctor = MethodReferenceAccessorFactory.createNodeFactory(FollowsOrder.class, null, new SelfAccessor(FollowsOrder.class));
        ctor.newInstance(1, "a");
    }

    @Test
    public void nodeConstructorArgsInDeclarationOrder() throws IllegalAccessException, SecurityException
    {
        final NodeFactory ctor = MethodReferenceAccessorFactory.createNodeFactory(FollowsDeclaration.class, null, new SelfAccessor(FollowsDeclaration.class));
        ctor.newInstance("a", 1);
    }

    @Test
    public void alkemizeIsInstrumented() throws IllegalAccessException, NoSuchMethodException, SecurityException, Throwable
    {
        assertThat(AlkemizerUtils.isInstrumented(clazz), is(true));
    }

    @Test
    public void alkemizeGetterSetter() throws IOException
    {
        final Set<String> methodNames = new HashSet<>();
        for (Method m : clazz.getMethods())
        {
            methodNames.add(m.getName());
        }
        assertThat(methodNames, hasItems("getFoo", "getBar", "setFoo", "setBar", "getIpsum", "setIpsum", "getDolor", "setDolor"));
    }

    @Test(expected = AlkemyException.class)
    public void testNodeConstructorWithArgs() throws NoSuchFieldException, SecurityException, IllegalAccessException
    {
        final NodeFactory ctor = MethodReferenceAccessorFactory.createNodeFactory(TestAlkemizer.class, null, new SelfAccessor(TestAlkemizer.class));
        final TestAlkemizer tc = (TestAlkemizer) ctor.newInstance(1, "foo");

        assertThat(1, is(tc.foo));
        assertThat("foo", is(tc.bar));

        // throws invalid argument ex.
        ctor.newInstance(1, 2, 3);
    }

    public void testNodeConstructorNoArgs() throws NoSuchFieldException, SecurityException, IllegalAccessException
    {
        final NodeFactory ctor = MethodReferenceAccessorFactory.createNodeFactory(TestAlkemizer.class, null, new SelfAccessor(TestAlkemizer.class));
        final TestAlkemizer tc = (TestAlkemizer) ctor.newInstance();

        assertThat(tc, is(not(nullValue())));
        assertThat(-1, is(tc.foo));
        assertThat("baz", is(tc.bar));
    }

    @Test
    public void testAccessorGetter() throws NoSuchFieldException, SecurityException, IllegalAccessException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(f);

        final TestAlkemizer tc = new TestAlkemizer();
        assertThat(-1, is(accessor.get(tc)));
    }

    @Test
    public void testAccessorSetter() throws NoSuchFieldException, SecurityException, IllegalAccessException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(f);

        final TestAlkemizer tc = new TestAlkemizer();
        accessor.set(1, tc);
        assertThat(1, is(accessor.get(tc)));
    }

    @Test
    public void testWidening() throws IllegalAccessException, SecurityException, NoSuchFieldException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(f);

        short s = 1;
        accessor.set(s, new TestAlkemizer());

        final AutoCastValueAccessor reflectAccessor = MethodReferenceAccessorFactory.createReflectiveValueAccessor(f);
        reflectAccessor.set(s, new TestAlkemizer());
    }

    @Test
    public void testNarrowing() throws IllegalAccessException, SecurityException, NoSuchFieldException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(f);

        double d = 1;
        accessor.set(d, new TestAlkemizer());

        final AutoCastValueAccessor reflectAccessor = MethodReferenceAccessorFactory.createReflectiveValueAccessor(f);
        reflectAccessor.set(d, new TestAlkemizer());
    }

    @Test
    public void testEnums() throws IllegalAccessException, SecurityException, NoSuchFieldException, AlkemyException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("ipsum");
        final TestAlkemizer ta = new TestAlkemizer();
        MethodReferenceAccessorFactory.createInstrumentedValueAccessor(f).set("ipsum", ta);
        assertThat(ta.ipsum, is(Lorem.ipsum));
    }

    @Test(expected = AlkemyException.class)
    public void testNoDefaultCtor()
    {
        AlkemyNodes.get(TestCreatedDefaultCtor.class);
    }

    @Test
    public void performanceAccesingStrategies() throws Throwable
    {
        System.out.println("**** Compare accessing / creating fiels (1e7 iterations) ****");

        final Field field = clazz.getDeclaredField("bar");
        final Method method = clazz.getDeclaredMethod("getBar");
        final MethodHandle handle = methodHandle(clazz, "getBar");
        final Function<Object, String> function = getterReference(handle, Object.class, String.class);
        final ValueAccessor barAccessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(clazz.getDeclaredField("bar"));
        final ValueAccessor fooAccessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(clazz.getDeclaredField("foo"));
        final ValueAccessor ipsumAccessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(clazz
                .getDeclaredField("ipsum"));
        final ValueAccessor dolorAccessor = MethodReferenceAccessorFactory.createInstrumentedValueAccessor(clazz
                .getDeclaredField("dolor"));
        final NodeFactory ctortc = MethodReferenceAccessorFactory.createNodeFactory(TestAlkemizer.class, null, new SelfAccessor(TestAlkemizer.class));
        final NodeFactory ctortmf = MethodReferenceAccessorFactory.createNodeFactory(TestManyFields.class, null, new SelfAccessor(TestManyFields.class));

        final TestAlkemizer tc = new TestAlkemizer();

        // warm up
        for (int i = 0; i < ITER; i++)
        {
            @SuppressWarnings("unused")
            String bar = (String) handle.invokeExact(tc);
            bar = (String) handle.invoke(tc);
            bar = function.apply(tc);
            bar = (String) method.invoke(tc);
            bar = (String) barAccessor.get(tc);
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

        System.out.println("Accessor get (TestClass)String: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(barAccessor.get(tc));
            }
        }) / 1000000 + " ms");

        System.out.println("Reflection (get): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(field.get(tc));
            }
        }) / 1000000 + " ms");

        System.out.println("Accessor set (TestClass)String: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                barAccessor.set("foo", tc);
            }
        }) / 1000000 + " ms");

        System.out.println("Reflection (set): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                field.set(tc, "foo");
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

        System.out.println("create$$instance (TestAlkemizer): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                ctortc.newInstance(1, "two", "ipsum", 1f);
            }
        }) / 1000000 + " ms");

        System.out.println("default ctor + sets (TestAlkemizer): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                final Object instance = ctortc.newInstance();
                fooAccessor.set(1, instance);
                barAccessor.set("two", instance);
                ipsumAccessor.set("ipsum", instance);
                dolorAccessor.set(1f, instance);
            }
        }) / 1000000 + " ms");

        System.out.println("Enum conversion: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                AlkemyUtils.toEnum(Lorem.class, "ipsum");
            }
        }) / 1000000 + " ms");

        final Object[] fields = new Object[26];
        Arrays.fill(fields, 1);
        System.out.println("create$$instance (TestManyFields: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                ctortmf.newInstance(fields);
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
        final MethodHandle handle = methodHandle(clazz, "getFoo");
        final Function<TestAlkemizer, Integer> f1 = getterReference(handle, TestAlkemizer.class,
                Integer.class);
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

        return (ObjIntFunction<T>) LambdaMetafactory.metafactory(MethodHandles.lookup(), funcMethod.getName(),
                MethodType.methodType(ObjIntFunction.class), funcType, handle, handle.type()).getTarget().invoke();
    }

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> Function<T, R> getterReference(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException,
            SecurityException, NoSuchMethodException
    {
        if (handle != null)
        {
            try
            {
                return MethodReferenceHelper.methodReference(Function.class, Function.class.getMethod("apply", Object.class), handle);
            }
            catch (SecurityException e)
            {
                throw e;
            }
            catch (NoSuchMethodException e)
            {
                throw e;
            }
        }
        return null;
    }
}
