package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjFloatConsumer;
import org.alkemy.functional.ToFloatFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class FloatReference extends LambdaReference implements ValueAccessor
{
    private final ToFloatFunction<Object> fgetter;
    private final ObjFloatConsumer<Object> fsetter;

    @SuppressWarnings("unchecked")
    public FloatReference(Field f) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, float.class);
        final Method accept = ObjFloatConsumer.class.getMethod("accept", Object.class, float.class);
        fsetter = MethodReferenceFactory.methodReference(ObjFloatConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToFloatFunction.class.getMethod("apply", Object.class);
        fgetter = MethodReferenceFactory.methodReference(ToFloatFunction.class, apply, getterHandle);

    }

    public float getFloat(Object parent)
    {
        return fgetter.apply(parent);
    }

    @Override
    public void set(float value, Object parent) throws AlkemyException
    {
        fsetter.accept(parent, value);
    }
}
