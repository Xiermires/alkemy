package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjShortConsumer;
import org.alkemy.functional.ToShortFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class ShortReference extends LambdaReference implements ValueAccessor
{
    private final ToShortFunction<Object> sgetter;
    private final ObjShortConsumer<Object> ssetter;

    @SuppressWarnings("unchecked")
    public ShortReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, short.class);
        final Method accept = ObjShortConsumer.class.getMethod("accept", Object.class, short.class);
        ssetter = MethodReferenceFactory.methodReference(ObjShortConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToShortFunction.class.getMethod("apply", Object.class);
        sgetter = MethodReferenceFactory.methodReference(ToShortFunction.class, apply, getterHandle);

    }

    public short getShort(Object parent)
    {
        return sgetter.apply(parent);
    }

    @Override
    public void set(short value, Object parent) throws AlkemyException
    {
        ssetter.accept(parent, value);
    }
}
