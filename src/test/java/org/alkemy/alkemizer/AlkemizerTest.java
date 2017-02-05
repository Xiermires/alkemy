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
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.alkemy.global.Helper;
import org.alkemy.global.Measure;
import org.alkemy.global.Measure.Measurable;
import org.alkemy.global.ObjIntFunction;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * These tests can only run individually. TODO Fix CL.
 */
public class AlkemizerTest
{
    @Test
    public void alkemizeTestClass() throws IOException
    {
        final Class<?> clazz = alkemize("org.alkemy.alkemizer.TestClass");

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
        final Class<?> clazz = alkemize("org.alkemy.alkemizer.TestClass");
        final MethodHandle handle = methodHandle(clazz, "get$$foo");

        final TestClass tc = (TestClass) clazz.newInstance();
        assertThat(-1, is((int) handle.invokeExact(tc)));
        assertThat(-1, is(handle.invoke(tc)));
    }

    @Test
    public void tryLambdaMetaFactory() throws Throwable
    {
        final Class<?> clazz = alkemize("org.alkemy.alkemizer.TestClass");
        final Function<Object, Integer> f = lambdaRef(methodHandle(clazz, "get$$foo"), Object.class, int.class);

        final TestClass tc = (TestClass) clazz.newInstance();
        assertThat(-1, is(f.apply(tc)));
    }

    @Test
    public void performanceMeasurements() throws Throwable
    {
        final Class<?> clazz = alkemize("org.alkemy.alkemizer.TestClass");
        final MethodHandle handle = methodHandle(clazz, "get$$bar");
        final Method method = clazz.getDeclaredMethod("get$$bar");
        final Function<Object, String> function = lambdaRef(handle, Object.class, String.class);

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
        final Class<?> clazz = alkemize("org.alkemy.alkemizer.TestClass");
        final MethodHandle handle = methodHandle(clazz, "get$$foo");
        final Function<TestClass, Integer> f1 = lambdaRef(handle, TestClass.class, Integer.class);
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

    private Class<?> alkemize(String clazz) throws IOException
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

    private <T, R> Function<T, R> lambdaRef(MethodHandle handle, Class<T> t, Class<R> r) throws Throwable
    {
        final MethodType func = handle.type();
        final CallSite site = LambdaMetafactory.metafactory(MethodHandles.lookup(), "apply", MethodType.methodType(Function.class), func.generic(), handle, func);
        final MethodHandle factory = site.getTarget();
        return (Function<T, R>) factory.invoke();
    }

    // FIXME AbstractMethodError. 
    private <T> ObjIntFunction<T> objIntLambdaRef(MethodHandle handle, Class<T> t) throws Throwable
    {
        final MethodType func = handle.type();
        final CallSite site = LambdaMetafactory.metafactory(MethodHandles.lookup(), "apply", MethodType.methodType(ObjIntFunction.class), func, handle, func);
        final MethodHandle factory = site.getTarget();
        return (ObjIntFunction<T>) factory.invoke();
    }
}
