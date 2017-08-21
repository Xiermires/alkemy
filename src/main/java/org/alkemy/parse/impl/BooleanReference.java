package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjBooleanConsumer;
import org.alkemy.functional.ToBooleanFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class BooleanReference extends LambdaReference implements ValueAccessor
{
    private final ToBooleanFunction<Object> zgetter;
    private final ObjBooleanConsumer<Object> zsetter;

    @SuppressWarnings("unchecked")
    public BooleanReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, boolean.class);
        final Method accept = ObjBooleanConsumer.class.getMethod("accept", Object.class, boolean.class);
        zsetter = MethodReferenceFactory.methodReference(ObjBooleanConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToBooleanFunction.class.getMethod("apply", Object.class);
        zgetter = MethodReferenceFactory.methodReference(ToBooleanFunction.class, apply, getterHandle);

    }

    public boolean getBoolean(Object parent)
    {
        return zgetter.apply(parent);
    }

    @Override
    public void set(boolean value, Object parent) throws AlkemyException
    {
        zsetter.accept(parent, value);
    }
}
