package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ObjLongConsumer;
import java.util.function.ToLongFunction;

import org.alkemy.exception.AlkemyException;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class LongReference extends LambdaReference implements ValueAccessor
{
    private final ToLongFunction<Object> jgetter;
    private final ObjLongConsumer<Object> jsetter;

    @SuppressWarnings("unchecked")
    public LongReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, long.class);
        final Method accept = ObjLongConsumer.class.getMethod("accept", Object.class, long.class);
        jsetter = MethodReferenceFactory.methodReference(ObjLongConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToLongFunction.class.getMethod("apply", Object.class);
        jgetter = MethodReferenceFactory.methodReference(ToLongFunction.class, apply, getterHandle);
    }

    public long getLong(Object parent)
    {
        return jgetter.applyAsLong(parent);
    }

    @Override
    public void set(long value, Object parent) throws AlkemyException
    {
        jsetter.accept(parent, value);
    }
}
