package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.alkemy.exception.AlkemyException;
import org.alkemy.instr.AlkemizerUtils;

public class LambdaReference extends Reference
{
    // All types.
    private final Function<Object, ?> getter;
    private final BiConsumer<Object, Object> setter;

    @SuppressWarnings("unchecked")
    LambdaReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        final Method apply = Function.class.getMethod("apply", Object.class);
        final Method accept = BiConsumer.class.getMethod("accept", Object.class, Object.class);

        final Class<?> clazz = f.getDeclaringClass();
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(clazz, AlkemizerUtils.getGetterName(f.getName()));
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(clazz, AlkemizerUtils.getSetterName(f.getName()), f.getType());

        final Function<Object, ?> getter = MethodReferenceFactory.methodReference(Function.class, apply, getterHandle);
        final BiConsumer<Object, Object> setter = MethodReferenceFactory.methodReference(BiConsumer.class, accept, setterHandle);

        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        return getter.apply(parent);
    }

    @Override
    public void set(Object value, Object parent) throws AlkemyException
    {
        setter.accept(parent, value);
    }
}
