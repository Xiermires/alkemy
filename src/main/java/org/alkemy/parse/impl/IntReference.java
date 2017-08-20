package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

import org.alkemy.exception.AlkemyException;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class IntReference extends ObjectReference implements ValueAccessor
{
    private final ToIntFunction<Object> igetter;
    private final ObjIntConsumer<Object> isetter;

    @SuppressWarnings("unchecked")
    public IntReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);

        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle isetterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, int.class);
        final Method iaccept = ObjIntConsumer.class.getMethod("accept", Object.class, int.class);
        isetter = MethodReferenceFactory.methodReference(ObjIntConsumer.class, iaccept, isetterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle igetterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method iapply = ToIntFunction.class.getMethod("applyAsInt", Object.class);
        igetter = MethodReferenceFactory.methodReference(ToIntFunction.class, iapply, igetterHandle);
    }

    public int getInt(Object parent)
    {
        return igetter.applyAsInt(parent);
    }

    @Override
    public void set(int value, Object parent) throws AlkemyException
    {
        isetter.accept(parent, value);
    }
}
