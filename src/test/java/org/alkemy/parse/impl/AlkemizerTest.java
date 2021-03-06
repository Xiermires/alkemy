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
import java.util.function.ToDoubleFunction;

import org.alkemy.Alkemy;
import org.alkemy.AlkemyMap;
import org.alkemy.TestAlkemyAccessor;
import org.alkemy.annotations.Order;
import org.alkemy.exception.AlkemyException;
import org.alkemy.instr.ConstructorWriter;
import org.alkemy.parse.AutoCastValueAccessor;
import org.alkemy.parse.NodeFactory;
import org.alkemy.parse.ValueAccessor;
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
    public void alkemizeCreateArgsMethod() throws IOException, NoSuchMethodException, SecurityException
    {
        final Method m = clazz.getMethod(ConstructorWriter.CREATE_ARGS, Object[].class);
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
    public void createArgsPreserveOrder() throws IllegalAccessException, SecurityException
    {
        final NodeFactory ctor = MethodReferenceFactory.createReferencedNodeFactory(new SelfAccessor(FollowsOrder.class));
        ctor.newInstance(1f, "a", 1);
    }

    @Test
    public void createArgsInDeclarationOrder() throws IllegalAccessException, SecurityException
    {
        final NodeFactory ctor = MethodReferenceFactory.createReferencedNodeFactory(new SelfAccessor(FollowsDeclaration.class));
        ctor.newInstance("a", 1, 1f);
    }

    @Test
    public void alkemizeIsInstrumented() throws IllegalAccessException, NoSuchMethodException, SecurityException, Throwable
    {
        assertThat(MethodReferenceFactory.isInstrumented(clazz), is(true));
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
        final NodeFactory ctor = MethodReferenceFactory.createReferencedNodeFactory(new SelfAccessor(TestAlkemizer.class));
        final TestAlkemizer tc = (TestAlkemizer) ctor.newInstance(1, "foo");

        assertThat(1, is(tc.foo));
        assertThat("foo", is(tc.bar));

        // throws invalid argument ex.
        ctor.newInstance(1, 2, 3);
    }

    public void testNodeConstructorNoArgs() throws NoSuchFieldException, SecurityException, IllegalAccessException
    {
        final NodeFactory ctor = MethodReferenceFactory.createReferencedNodeFactory(new SelfAccessor(TestAlkemizer.class));
        final TestAlkemizer tc = (TestAlkemizer) ctor.newInstance();

        assertThat(tc, is(not(nullValue())));
        assertThat(-1, is(tc.foo));
        assertThat("baz", is(tc.bar));
    }

    @Test
    public void testAccessorGetter() throws NoSuchFieldException, SecurityException, IllegalAccessException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceFactory.createReferencedValueAccessor(f);

        final TestAlkemizer tc = new TestAlkemizer();
        assertThat(-1, is(accessor.get(tc)));
    }

    @Test
    public void testAccessorSetter() throws NoSuchFieldException, SecurityException, IllegalAccessException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceFactory.createReferencedValueAccessor(f);

        final TestAlkemizer tc = new TestAlkemizer();
        accessor.set(1, tc);
        assertThat(1, is(accessor.get(tc)));
    }

    @Test
    public void testWidening() throws IllegalAccessException, SecurityException, NoSuchFieldException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceFactory.createReferencedValueAccessor(f);

        short s = 1;
        accessor.set(s, new TestAlkemizer());

        final AutoCastValueAccessor reflectAccessor = MethodReferenceFactory.createReflectedValueAccessor(f);
        reflectAccessor.set(s, new TestAlkemizer());
    }

    @Test
    public void testNarrowing() throws IllegalAccessException, SecurityException, NoSuchFieldException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("foo");
        final AutoCastValueAccessor accessor = MethodReferenceFactory.createReferencedValueAccessor(f);

        double d = 1;
        accessor.set(d, new TestAlkemizer());

        final AutoCastValueAccessor reflectAccessor = MethodReferenceFactory.createReflectedValueAccessor(f);
        reflectAccessor.set(d, new TestAlkemizer());
    }

    @Test
    public void testEnums() throws IllegalAccessException, SecurityException, NoSuchFieldException, AlkemyException, NoSuchMethodException
    {
        final Field f = clazz.getDeclaredField("ipsum");
        final TestAlkemizer ta = new TestAlkemizer();
        MethodReferenceFactory.createReferencedValueAccessor(f).set("ipsum", ta);
        assertThat(ta.ipsum, is(Lorem.ipsum));
    }

    @Test
    public void testNoDefaultCtor()
    {
        Alkemy.parse(TestCreatedDefaultCtor.class);
    }

    @Test
    public void performanceAccesingStrategies() throws Throwable
    {
        final Field field = clazz.getDeclaredField("bar");
        field.setAccessible(true);
        final Method method = clazz.getDeclaredMethod("getBar");
        method.setAccessible(true);
        final MethodHandle handle = methodHandle(clazz, "getBar");
        final Function<Object, String> function = getterReference(handle, Object.class, String.class);
        final ValueAccessor barAccessor = MethodReferenceFactory.createReferencedValueAccessor(clazz.getDeclaredField("bar"));
        final ValueAccessor fooAccessor = MethodReferenceFactory.createReferencedValueAccessor(clazz.getDeclaredField("foo"));
        final ValueAccessor ipsumAccessor = MethodReferenceFactory.createReferencedValueAccessor(clazz.getDeclaredField("ipsum"));
        final ValueAccessor dolorAccessor = MethodReferenceFactory.createReferencedValueAccessor(clazz.getDeclaredField("dolor"));
        final NodeFactory ctortc = MethodReferenceFactory.createReferencedNodeFactory(new SelfAccessor(TestAlkemizer.class));
        final NodeFactory ctortmf = MethodReferenceFactory.createReferencedNodeFactory(new SelfAccessor(TestManyFields.class));
        final ToDoubleFunction<TestAlkemizer> lambda = l -> l.getDolor();

        final TestAlkemizer testee = new TestAlkemizer();

        final Field foo = TestAlkemizer.class.getDeclaredField("foo");
        final IntReference iref = new IntReference(foo);
        final ValueAccessor reflectedFooAccessor = new ReflectedReference(foo);

        // warm up
        for (int i = 0; i < 1000; i++)
        {
            @SuppressWarnings("unused")
            String bar = (String) handle.invokeExact(testee);
            handle.invoke(testee);
            function.apply(testee);
            method.invoke(testee);
            barAccessor.get(testee);
            fooAccessor.getInt(testee);
            reflectedFooAccessor.getInt(testee);
            barAccessor.set("foo", testee);
            iref.getInt(testee);
            fooAccessor.set(1, testee);
            lambda.applyAsDouble(testee);
        }

        System.out.println("MethodHandle#invokeExact(): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                @SuppressWarnings("unused")
                String bar = (String) handle.invokeExact(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("MethodHandle#invoke(): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(handle.invoke(testee));
            }
        }) / 1000000 + " ms");

        System.out.println("LambdaMetaFactory (TestClass)String: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                function.apply(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("Accessor get (TestClass)String: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                barAccessor.get(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("Accessor get (TestClass)int: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                fooAccessor.getInt(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("Reflected accessor get (TestClass)int: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                reflectedFooAccessor.getInt(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("Accessor iref (TestClass)int: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                iref.getInt(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("Reflection (field.get): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(field.get(testee));
            }
        }) / 1000000 + " ms");

        System.out.println("Accessor set (TestClass)String: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                barAccessor.set("foo", testee);
            }
        }) / 1000000 + " ms");

        System.out.println("Accessor set (TestClass)int: " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                fooAccessor.set(1, testee);
            }
        }) / 1000000 + " ms");

        System.out.println("Reflection (field.set): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                field.set(testee, "foo");
            }
        }) / 1000000 + " ms");

        System.out.println("Reflection (getXXX()): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                String.class.cast(method.invoke(testee));
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

        System.out.println("Lambda (getter): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                lambda.applyAsDouble(testee);
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
        System.out.println(ConstructorWriter.CREATE_ARGS + "(TestManyFields): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                ctortmf.newInstance(fields);
            }
        }) / 1000000 + " ms");

        System.out.println("no-args ctor + set(value, parent) (TestAlkemizer): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                final Object instance = ctortc.newInstance();
                fooAccessor.set(1, instance);
                barAccessor.set("two", instance);
                ipsumAccessor.set(Lorem.ipsum, instance);
                dolorAccessor.set(1f, instance);
            }
        }) / 1000000 + " ms");

        System.out.println(ConstructorWriter.CREATE_ARGS + "(TestAlkemizer): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                ctortc.newInstance(1, "two", Lorem.ipsum, 1f);
            }
        }) / 1000000 + " ms");

        System.out.println("Direct access (TestAlkemizerCompiledVersion.create$$args): " + Measure.measure(() ->
        {
            for (int i = 0; i < ITER; i++)
            {
                TestAlkemizerCompiledVersion.create$$args(1, "two", Lorem.ipsum, 1f);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceAlkemyElementGet() throws Throwable
    {
        final TestAlkemyAccessor testee = new TestAlkemyAccessor();
        final AlkemyMap map = new AlkemyMap(Alkemy.parse(TestAlkemyAccessor.class));
        final AlkemyElement d = map.get(TestAlkemyAccessor.class, "d");
        final AlkemyElement f = map.get(TestAlkemyAccessor.class, "f");
        final AlkemyElement j = map.get(TestAlkemyAccessor.class, "j");
        final AlkemyElement i = map.get(TestAlkemyAccessor.class, "i");
        final AlkemyElement s = map.get(TestAlkemyAccessor.class, "s");
        final AlkemyElement c = map.get(TestAlkemyAccessor.class, "c");
        final AlkemyElement b = map.get(TestAlkemyAccessor.class, "b");
        final AlkemyElement z = map.get(TestAlkemyAccessor.class, "z");
        final AlkemyElement str = map.get(TestAlkemyAccessor.class, "str");
        final AlkemyElement o = map.get(TestAlkemyAccessor.class, "o");

        System.out.println("AlkemyElement#getDouble: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                d.getDouble(testee);
            }
        }) / 1000000 + " ms");
        
        System.out.println("AlkemyElement#getFloat: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                f.getFloat(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#getLong: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                j.getLong(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#getInt: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                i.getInt(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#getShort: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                s.getShort(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#getChar: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                c.getChar(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#getByte: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                b.getByte(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#getBoolean: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                z.getBoolean(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#getString: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                str.getString(testee);
            }
        }) / 1000000 + " ms");

        System.out.println("AlkemyElement#get: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                o.get(testee);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void performanceAlkemyElementSet() throws Throwable
    {
        final TestAlkemyAccessor testee = new TestAlkemyAccessor();
        final AlkemyMap map = new AlkemyMap(Alkemy.parse(TestAlkemyAccessor.class));
        final AlkemyElement d = map.get(TestAlkemyAccessor.class, "d");
        final AlkemyElement f = map.get(TestAlkemyAccessor.class, "f");
        final AlkemyElement j = map.get(TestAlkemyAccessor.class, "j");
        final AlkemyElement i = map.get(TestAlkemyAccessor.class, "i");
        final AlkemyElement s = map.get(TestAlkemyAccessor.class, "s");
        final AlkemyElement c = map.get(TestAlkemyAccessor.class, "c");
        final AlkemyElement b = map.get(TestAlkemyAccessor.class, "b");
        final AlkemyElement z = map.get(TestAlkemyAccessor.class, "z");
        final AlkemyElement str = map.get(TestAlkemyAccessor.class, "str");
        final AlkemyElement o = map.get(TestAlkemyAccessor.class, "o");

        System.out.println("AlkemyElement#setDouble: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                d.set(1d, testee);
            }
        }) / 1000000 + " ms");
        
        System.out.println("AlkemyElement#setFloat: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                f.set(1f, testee);
            }
        }) / 1000000 + " ms");
        
        System.out.println("AlkemyElement#setLong: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                j.set(1l, testee);
            }
        }) / 1000000 + " ms");
        
        System.out.println("AlkemyElement#setInt: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                i.set(1, testee);
            }
        }) / 1000000 + " ms");
        
        final short _short = 1;
        System.out.println("AlkemyElement#setShort: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                s.set(_short, testee);
            }
        }) / 1000000 + " ms");
        
        System.out.println("AlkemyElement#setChar: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                c.set('a', testee);
            }
        }) / 1000000 + " ms");
        
        final byte _byte = 1;
        System.out.println("AlkemyElement#setByte: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                b.set(_byte, testee);
            }
        }) / 1000000 + " ms");
        
        System.out.println("AlkemyElement#setBoolean: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                z.set(true, testee);
            }
        }) / 1000000 + " ms");
        
        System.out.println("AlkemyElement#setString: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                str.set("bar", testee);
            }
        }) / 1000000 + " ms");
        
        final Object _obj = new Object();
        System.out.println("AlkemyElement#set: " + Measure.measure(() ->
        {
            for (int k = 0; k < ITER; k++)
            {
                o.set(_obj, testee);
            }
        }) / 1000000 + " ms");
    }

    @Test
    public void compareLambdaRefObjectvsRefPrimitive() throws Throwable
    {
        final MethodHandle handle = methodHandle(clazz, "getFoo");
        final Function<TestAlkemizer, Integer> f1 = getterReference(handle, TestAlkemizer.class, Integer.class);
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

    private MethodHandle methodHandle(Class<?> clazz, String methodName) throws IllegalAccessException, NoSuchMethodException, SecurityException
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

        return (ObjIntFunction<T>) LambdaMetafactory.metafactory(MethodHandles.lookup(), funcMethod.getName(), MethodType.methodType(ObjIntFunction.class), funcType, handle, handle.type())
                .getTarget().invoke();
    }

    @SuppressWarnings("unchecked")
    // safe
    static <T, R> Function<T, R> getterReference(MethodHandle handle, Class<T> clazz, Class<R> r) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        if (handle != null)
        {
            try
            {
                return MethodReferenceFactory.methodReference(Function.class, Function.class.getMethod("apply", Object.class), handle);
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
