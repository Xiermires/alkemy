package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

import org.alkemy.exception.AlkemyException;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class DoubleReference extends ObjectReference implements ValueAccessor
{
    private final ToDoubleFunction<Object> dgetter;
    private final ObjDoubleConsumer<Object> dsetter;

    @SuppressWarnings("unchecked")
    public DoubleReference(Field f) throws IllegalAccessException, SecurityException, NoSuchMethodException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, double.class);
        final Method accept = ObjDoubleConsumer.class.getMethod("accept", Object.class, double.class);
        dsetter = MethodReferenceFactory.methodReference(ObjDoubleConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToDoubleFunction.class.getMethod("apply", Object.class);
        dgetter = MethodReferenceFactory.methodReference(ToDoubleFunction.class, apply, getterHandle);
    }

    public double getDouble(Object parent)
    {
        return dgetter.applyAsDouble(parent);
    }

    @Override
    public void set(double value, Object parent) throws AlkemyException
    {
        dsetter.accept(parent, value);
    }
}
