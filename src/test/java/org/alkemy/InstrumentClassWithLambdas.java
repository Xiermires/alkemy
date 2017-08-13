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
package org.alkemy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.agenttools.Agents;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * A mirrored version of the JUnit {@link Suite} that defines classes as strings instead of {@link Class} to avoid the class loading.
 * <p>
 * Motivation is to allow test classes with instrumentation to succeed.
 * <p>
 * When the JVM loads a class containing lambdas, it translates them before any other action. <br>
 * The translation process includes loading the classes affecting the lambda, which ends up loading the soon-to-be-instrumented classes. <br>
 * If instrumentation implies creating need methods, fields, etc. if will inevitably fail.
 * <p>
 * By wrapping the offending test classes in a Suite, we can force the instrumentation in the suite before the class is loaded.
 */
public class InstrumentClassWithLambdas extends Suite
{
    public InstrumentClassWithLambdas(Class<?> klass, RunnerBuilder builder) throws InitializationError
    {
        super(builder, klass, instrumentGetClassNames(klass));
    }

    private static Class<?>[] instrumentGetClassNames(Class<?> klass) throws InitializationError
    {
        final InstrumentableLambdaClasses annotation = klass.getAnnotation(InstrumentableLambdaClasses.class);
        if (annotation == null) { throw new InitializationError(String.format("class '%s' must have a InstrumentableLambdaClasses annotation", klass.getName())); }
        final List<Instr> instrs = Arrays.asList(annotation.instrs());

        instrs.forEach(instr -> Arrays.asList(instr.ctf()).forEach(ctf ->
        {
            try
            {
                Agents.retransform(ctf.newInstance(), instr.classNames());
            }
            catch (Exception e)
            {
                throw new RuntimeException(new InitializationError(String.format("Unable to load the class file transformer '%s'", Arrays.asList(instr.classNames()))));
            }
        }));

        return Arrays.asList(annotation.testClassNames()).stream().map(s ->
        {
            try
            {
                return Class.forName(s);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()).toArray(new Class[0]);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface InstrumentableLambdaClasses
    {
        /**
         * @return the classes to be run
         */
        public String[] testClassNames();

        // The class names to instrument
        public Instr[] instrs();
    }

    public @interface Instr
    {
        String[] classNames();

        Class<? extends ClassFileTransformer>[] ctf();
    }
}
