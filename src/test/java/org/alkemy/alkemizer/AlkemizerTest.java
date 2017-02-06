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
package org.alkemy.alkemizer;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.alkemy.general.Helper;
import org.alkemy.general.Measure;
import org.alkemy.general.Measure.Measurable;
import org.alkemy.general.ObjIntFunction;
import org.alkemy.methodhandle.MethodHandleAccessorFactory;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * These tests can only run individually. TODO Fix CL.
 */
public class AlkemizerTest
{
    @Test
    public void alkemizeIsInstrumented() throws IllegalAccessException, NoSuchMethodException, SecurityException, Throwable
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        final Supplier<Boolean> s = MethodHandleAccessorFactory.ref2StaticGetter(methodHandle(clazz, "is$$instrumented"), clazz, boolean.class);

        assertThat(s.get(), is(true));
    }

    @Test
    public void alkemizeGetterSetter() throws IOException
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");

        final Set<String> methodNames = new HashSet<>();
        for (Method m : clazz.getMethods())
        {
            methodNames.add(m.getName());
        }
        assertThat(methodNames, hasItems("get$$foo", "get$$bar", "set$$foo", "set$$bar"));
    }

    @Test
    public void tryMethodhandle() throws Throwable
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        final MethodHandle handle = methodHandle(clazz, "get$$foo");

        final TestClass tc = (TestClass) clazz.newInstance();
        assertThat(-1, is((int) handle.invokeExact(tc)));
        assertThat(-1, is(handle.invoke(tc)));
    }

    @Test
    public void tryLambdaMetaFactoryGetter() throws Throwable
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        final MethodHandle methodHandle = MethodHandleAccessorFactory.methodHandle(clazz, "get$$foo");
        final Function<Object, Integer> f = MethodHandleAccessorFactory.ref2MemberGetter(methodHandle, Object.class, int.class);

        final TestClass tc = (TestClass) clazz.newInstance();
        assertThat(-1, is(f.apply(tc)));
    }

    @Test
    public void tryLambdaMetaFactoryStaticGetter() throws Throwable
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        final MethodHandle methodHandle = MethodHandleAccessorFactory.methodHandle(clazz, Alkemizer.IS_INSTRUMENTED);
        final Supplier<Boolean> f = MethodHandleAccessorFactory.ref2StaticGetter(methodHandle, Object.class, boolean.class);

        assertThat(true, is(f.get()));
    }

    @Test
    public void tryLambdaMetaFactorySetter() throws Throwable
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        final MethodHandle getterHandle = MethodHandleAccessorFactory.methodHandle(clazz, Alkemizer.getGetterName("foo"));
        final MethodHandle setterHandle = MethodHandleAccessorFactory.methodHandle(clazz, Alkemizer.getSetterName("foo"), int.class);
        final Function<TestClass, Integer> getter = MethodHandleAccessorFactory.ref2MemberGetter(getterHandle, TestClass.class, int.class);
        final BiConsumer<TestClass, Integer> setter = MethodHandleAccessorFactory.ref2MemberSetter(setterHandle, TestClass.class, int.class);

        final TestClass tc = (TestClass) clazz.newInstance();
        setter.accept(tc, 1);

        assertThat(1, is(getter.apply(tc)));
    }

    @Test
    public void performanceMeasurements() throws Throwable
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        final MethodHandle handle = methodHandle(clazz, "get$$bar");
        final Method method = clazz.getDeclaredMethod("get$$bar");
        final Function<Object, String> function = MethodHandleAccessorFactory.ref2MemberGetter(handle, Object.class, String.class);

        final TestClass tc = (TestClass) clazz.newInstance();

        // warm up
        for (int i = 0; i < 1000000; i++)
        {
            @SuppressWarnings("unused")
            String bar = (String) handle.invokeExact(tc);
            bar = (String) handle.invoke(tc);
            bar = function.apply(tc);
            bar = (String) method.invoke(tc);
        }

        // invoke exact
        System.out.println(Measure.measure(new Measurable()
        {
            @Override
            public void start() throws Throwable
            {
                for (int i = 0; i < 10000000; i++)
                {
                    @SuppressWarnings("unused")
                    String bar = (String) handle.invokeExact(tc);
                }
            }
        }));

        // invoke
        System.out.println(Measure.measure(new Measurable()
        {
            @Override
            public void start() throws Throwable
            {
                for (int i = 0; i < 10000000; i++)
                {
                    @SuppressWarnings("unused")
                    String bar = String.class.cast(handle.invoke(tc));
                }
            }
        }));

        // lambda meta factory
        System.out.println(Measure.measure(new Measurable()
        {
            @Override
            public void start() throws Throwable
            {
                for (int i = 0; i < 10000000; i++)
                {
                    @SuppressWarnings("unused")
                    String bar = function.apply(tc);
                }
            }
        }));

        // reflection
        System.out.println(Measure.measure(new Measurable()
        {
            @Override
            public void start() throws Throwable
            {
                for (int i = 0; i < 10000000; i++)
                {
                    @SuppressWarnings("unused")
                    String bar = String.class.cast(method.invoke(tc));
                }
            }
        }));

        // direct access
        final TestClassExpanded tce = new TestClassExpanded(-1, "baz");
        System.out.println(Measure.measure(new Measurable()
        {
            @Override
            public void start() throws Throwable
            {
                for (int i = 0; i < 10000000; i++)
                {
                    @SuppressWarnings("unused")
                    String bar = tce.get$$bar();
                }
            }
        }));
    }

    @Test
    public void compareLambdaRefsPerformance() throws Throwable
    {
        final Class<?> clazz = AlkemizerTest.alkemize(getClass().getPackage().getName() + ".TestClass");
        final MethodHandle handle = methodHandle(clazz, "get$$foo");
        final Function<TestClass, Integer> f1 = MethodHandleAccessorFactory.ref2MemberGetter(handle, TestClass.class, Integer.class);
        final ObjIntFunction<TestClass> f2 = objIntLambdaRef(handle, TestClass.class);

        final TestClass tc = (TestClass) clazz.newInstance();

        // warm up
        for (int i = 0; i < 1000000; i++)
        {
            @SuppressWarnings("unused")
            int foo = f1.apply(tc);
            foo = f2.apply(tc);
        }

        // lambda meta factory
        System.out.println(Measure.measure(new Measurable()
        {
            @Override
            public void start() throws Throwable
            {
                for (int i = 0; i < 10000000; i++)
                {
                    @SuppressWarnings("unused")
                    int foo = f1.apply(tc);
                }
            }
        }));

        // lambda meta factory
        System.out.println(Measure.measure(new Measurable()
        {
            @Override
            public void start() throws Throwable
            {
                for (int i = 0; i < 10000000; i++)
                {
                    @SuppressWarnings("unused")
                    int foo = f2.apply(tc);
                }
            }
        }));
    }

    public static Class<?> alkemize(String clazz) throws IOException
    {
        final ClassReader cr = new ClassReader(clazz);
        final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(cw, ClassReader.SKIP_FRAMES);
        return Helper.loadClass(clazz, Alkemizer.alkemize(cw.toByteArray()));
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

        return (ObjIntFunction<T>) LambdaMetafactory
                .metafactory(MethodHandles.lookup(), funcMethod.getName(), MethodType.methodType(ObjIntFunction.class), funcType, handle, handle.type()).getTarget()
                .invoke();
    }
}
