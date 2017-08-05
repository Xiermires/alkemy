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

import static org.alkemy.parse.impl.MethodReferenceHelper.methodHandle;
import static org.alkemy.parse.impl.MethodReferenceHelper.methodReference;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.alkemy.functional.ObjBooleanConsumer;
import org.alkemy.functional.ObjByteConsumer;
import org.alkemy.functional.ObjCharConsumer;
import org.alkemy.functional.ObjFloatConsumer;
import org.alkemy.functional.ObjShortConsumer;
import org.alkemy.functional.ToByteFunction;
import org.alkemy.functional.ToCharFunction;
import org.alkemy.functional.ToFloatFunction;
import org.alkemy.functional.ToShortFunction;
import org.alkemy.util.Assertions;
import org.alkemy.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;

@SuppressWarnings("unchecked")
public class MethodReferenceAccessorFactory
{
    private static final Logger log = LoggerFactory.getLogger(MethodReferenceAccessorFactory.class);

    private static final Map<Class<?>, Pair<Class<?>, String>> primitiveGetters;
    private static final Map<Class<?>, Pair<Class<?>, String>> primitiveSetters;
    private static final Map<Class<?>, BiConsumer<StaticInstrumentAccessor, Object>> setPrimitiveGetters;
    private static final Map<Class<?>, BiConsumer<StaticInstrumentAccessor, Object>> setPrimitiveSetters;
    static
    {
        final Builder<Class<?>, Pair<Class<?>, String>> getters = ImmutableMap.builder();
        getters.put(double.class, Pair.create(ToDoubleFunction.class, "applyAsDouble"));
        getters.put(float.class, Pair.create(ToFloatFunction.class, "apply"));
        getters.put(long.class, Pair.create(ToLongFunction.class, "applyAsLong"));
        getters.put(int.class, Pair.create(ToIntFunction.class, "applyAsInt"));
        getters.put(short.class, Pair.create(ToShortFunction.class, "apply"));
        getters.put(char.class, Pair.create(ToCharFunction.class, "apply"));
        getters.put(byte.class, Pair.create(ToByteFunction.class, "apply"));
        getters.put(boolean.class, Pair.create(Predicate.class, "test"));
        primitiveGetters = getters.build();

        final Builder<Class<?>, Pair<Class<?>, String>> setters = ImmutableMap.builder();
        setters.put(double.class, Pair.create(ObjDoubleConsumer.class, "accept"));
        setters.put(float.class, Pair.create(ObjFloatConsumer.class, "accept"));
        setters.put(long.class, Pair.create(ObjLongConsumer.class, "accept"));
        setters.put(int.class, Pair.create(ObjIntConsumer.class, "accept"));
        setters.put(short.class, Pair.create(ObjShortConsumer.class, "accept"));
        setters.put(char.class, Pair.create(ObjCharConsumer.class, "accept"));
        setters.put(byte.class, Pair.create(ObjByteConsumer.class, "accept"));
        setters.put(boolean.class, Pair.create(ObjBooleanConsumer.class, "accept"));
        primitiveSetters = setters.build();

        final Builder<Class<?>, BiConsumer<StaticInstrumentAccessor, Object>> setSetters = ImmutableMap.builder();
        setSetters.put(double.class, (t1, t2) -> t1.doubleSetter((ObjDoubleConsumer<Object>) t2));
        setSetters.put(float.class, (t1, t2) -> t1.floatSetter((ObjFloatConsumer<Object>) t2));
        setSetters.put(long.class, (t1, t2) -> t1.longSetter((ObjLongConsumer<Object>) t2));
        setSetters.put(int.class, (t1, t2) -> t1.intSetter((ObjIntConsumer<Object>) t2));
        setSetters.put(short.class, (t1, t2) -> t1.shortSetter((ObjShortConsumer<Object>) t2));
        setSetters.put(char.class, (t1, t2) -> t1.charSetter((ObjCharConsumer<Object>) t2));
        setSetters.put(byte.class, (t1, t2) -> t1.byteSetter((ObjByteConsumer<Object>) t2));
        setSetters.put(boolean.class, (t1, t2) -> t1.booleanSetter((ObjBooleanConsumer<Object>) t2));
        setPrimitiveSetters = setSetters.build();

        final Builder<Class<?>, BiConsumer<StaticInstrumentAccessor, Object>> setGetters = ImmutableMap.builder();
        setGetters.put(double.class, (t1, t2) -> t1.doubleGetter((ToDoubleFunction<Object>) t2));
        setGetters.put(float.class, (t1, t2) -> t1.floatGetter((ToFloatFunction<Object>) t2));
        setGetters.put(long.class, (t1, t2) -> t1.longGetter((ToLongFunction<Object>) t2));
        setGetters.put(int.class, (t1, t2) -> t1.intGetter((ToIntFunction<Object>) t2));
        setGetters.put(short.class, (t1, t2) -> t1.shortGetter((ToShortFunction<Object>) t2));
        setGetters.put(char.class, (t1, t2) -> t1.charGetter((ToCharFunction<Object>) t2));
        setGetters.put(byte.class, (t1, t2) -> t1.byteGetter((ToByteFunction<Object>) t2));
        setGetters.put(boolean.class, (t1, t2) -> t1.booleanGetter((Predicate<Object>) t2));
        setPrimitiveGetters = setGetters.build();
    }

    static ValueAccessor createReflectiveValueAccessor(Field f) throws IllegalAccessException, SecurityException,
            NoSuchMethodException
    {
        return Modifier.isStatic(f.getModifiers()) ? new StaticReflectAccessor(f) : new MemberReflectAccessor(f);
    }


    static ValueAccessor createInstrumentedValueAccessor(Field f) throws IllegalAccessException, SecurityException,
            NoSuchMethodException
    {
        final Class<?> clazz = f.getDeclaringClass();
        final MethodHandle getterHandle = methodHandle(clazz, FieldAccessorWriter.getGetterName(f.getName()));
        final MethodHandle setterHandle = methodHandle(clazz, FieldAccessorWriter.getSetterName(f.getName()), f.getType());

        final StaticInstrumentAccessor valueAccessor = create(f, clazz, getterHandle, setterHandle);
        return createTypifiedInstrumentedAccessors(f, clazz, valueAccessor);
    }

    private static StaticInstrumentAccessor create(Field f, final Class<?> clazz, final MethodHandle getterHandle,
            final MethodHandle setterHandle) throws NoSuchMethodException, IllegalAccessException
    {
        final StaticInstrumentAccessor valueAccessor;
        final Method apply = Function.class.getMethod("apply", Object.class);
        final Method accept = BiConsumer.class.getMethod("accept", Object.class, Object.class);

        final Function<Object, Object> getter = methodReference(Function.class, apply, getterHandle);
        final BiConsumer<Object, Object> setter = methodReference(BiConsumer.class, accept, setterHandle);

        final String name = f.getDeclaringClass().getTypeName() + "." + f.getName();

        if (Modifier.isStatic(f.getModifiers()))
        {
            valueAccessor = new StaticInstrumentAccessor(name, f.getType(), getter, setter);
        }
        else
        {
            valueAccessor = new MemberInstrumentAccessor(name, f.getType(), getter, setter);
        }
        return valueAccessor;
    }

    private static StaticInstrumentAccessor createTypifiedInstrumentedAccessors(Field f, Class<?> clazz,
            StaticInstrumentAccessor valueAccessor) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        if (f.getType().isEnum())
        {
            // string to enum
            final MethodHandle setterHandle = methodHandle(clazz, FieldAccessorWriter.getSetterName(f.getName()), String.class);
            final Method accept = BiConsumer.class.getMethod("accept", Object.class, Object.class);
            valueAccessor.stringSetter(methodReference(BiConsumer.class, accept, setterHandle));
        }
        else if (f.getType().isPrimitive())
        {
            for (Class<?> primitive : primitiveSetters.keySet())
            {
                try
                {
                    final String setterName = FieldAccessorWriter.getSetterName(f.getName());
                    if (!primitive.equals(f.getType()))
                        setterName.concat(AlkemizerUtils.camelUp(primitive.getSimpleName()));
                    
                    final MethodHandle setterHandle = methodHandle(clazz, setterName, primitive);
                    final Pair<Class<?>, String> reference = primitiveSetters.get(primitive);
                    final Method method = reference.first.getDeclaredMethod(reference.second, Object.class, primitive);
                    setPrimitiveSetters.get(primitive).accept(valueAccessor,
                            methodReference(reference.first, method, setterHandle));
                }
                catch (SecurityException | ReflectiveOperationException e)
                {
                    if (!failed.containsKey(f))
                    {
                        log.trace("Field '{}' in type '{}' does not implement specific '{}' setter." //
                                , f.getName()//
                                , clazz.getName()//
                                , primitive.getSimpleName());
                        failed.put(Pair.create(f, primitive), null);
                    }
                }
            }

            try
            {
                final String getterName = FieldAccessorWriter.getGetterName(f.getName());
                final MethodHandle getterHandle = methodHandle(clazz, getterName);
                final Pair<Class<?>, String> reference = primitiveGetters.get(f.getType());
                final Method method = reference.first.getDeclaredMethod(reference.second, Object.class);
                setPrimitiveGetters.get(f.getType())
                        .accept(valueAccessor, methodReference(reference.first, method, getterHandle));
            }
            catch (SecurityException | ReflectiveOperationException e)
            {
                if (!failed.containsKey(f))
                {
                    log.trace("Field '{}' in type '{}' does not implement specific '{}' getter."//
                            , f.getName()//
                            , clazz.getName()//
                            , f.getType().getSimpleName());

                    failed.put(Pair.create(f, f.getType()), null);
                }
            }
        }
        return valueAccessor;
    }

    // Try avoiding logging several times the same class.
    // The assumption is the parsing of the method is faster than the GC claiming this objects.
    static final WeakHashMap<Pair<Field, Class<?>>, Object> failed = new WeakHashMap<>();

    static NodeFactory createNodeFactory(Class<?> clazz, Class<?> componentTypeClass, AutoCastValueAccessor valueAccessor)
            throws IllegalAccessException, SecurityException
    {
        // If an alkemizable class extends another alkemizable class, it receives two static
        // methods. Get the explicit one of this type.
        Method factoryMethod = null;
        for (Method m : Iterables.filter(Arrays.asList(clazz.getMethods())//
                , f -> FieldOrderWriter.CREATE_INSTANCE.equals(f.getName())))
            if (m.getReturnType().equals(clazz))
                factoryMethod = m;

        Assertions.nonNull(factoryMethod);

        MethodHandle factory = null;
        try
        {
            factory = methodHandle(clazz, FieldOrderWriter.CREATE_INSTANCE, factoryMethod.getParameterTypes());
        }
        catch (NoSuchMethodException e)
        {
            log.debug("Factory method not present in type '{}'.", clazz.getName());
        }

        try
        {
            final Method get = Supplier.class.getMethod("get");
            final MethodHandle ctorClass = MethodHandles.lookup().unreflectConstructor(clazz.getConstructor());
            final Supplier<Object> classCtor = methodReference(Supplier.class, get, ctorClass);
            final Method factoryReference = ConstructorFunction.class.getMethod("newInstance", Object[].class);
            final ConstructorFunction factoryWithArgs = methodReference(ConstructorFunction.class, factoryReference, factory);

            final Supplier<Object> componentClassCtor;
            if (componentTypeClass != null)
            {
                final MethodHandle ctorComponentClass = MethodHandles.lookup()//
                        .unreflectConstructor(componentTypeClass.getConstructor());

                componentClassCtor = methodReference(Supplier.class, get, ctorComponentClass);
            }
            else
            {
                componentClassCtor = null;
            }

            return new StaticMethodLambdaBasedConstructor(//
                    clazz//
                    , componentTypeClass//
                    , classCtor//
                    , componentClassCtor//
                    , factoryWithArgs//
                    , valueAccessor);
        }
        catch (NoSuchMethodException e)
        {
            log.debug("Method not found. Can't use lambdas.", e);
        }
        return null;
    }
}
