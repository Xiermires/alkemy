package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjByteConsumer;
import org.alkemy.functional.ToByteFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class ByteReference extends LambdaReference implements ValueAccessor
{
    private final ToByteFunction<Object> bgetter;
    private final ObjByteConsumer<Object> bsetter;

    @SuppressWarnings("unchecked")
    public ByteReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, byte.class);
        final Method accept = ObjByteConsumer.class.getMethod("accept", Object.class, byte.class);
        bsetter = MethodReferenceFactory.methodReference(ObjByteConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToByteFunction.class.getMethod("apply", Object.class);
        bgetter = MethodReferenceFactory.methodReference(ToByteFunction.class, apply, getterHandle);
    }

    public byte getByte(Object parent)
    {
        return bgetter.apply(parent);
    }

    @Override
    public void set(byte value, Object parent) throws AlkemyException
    {
        bsetter.accept(parent, value);
    }
}