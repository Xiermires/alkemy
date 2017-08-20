package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjStringConsumer;
import org.alkemy.functional.ToStringFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class StringReference extends ObjectReference implements ValueAccessor
{
    private final ToStringFunction<Object> sgetter;
    private final ObjStringConsumer<Object> ssetter;

    @SuppressWarnings("unchecked")
    public StringReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, String.class);
        final Method accept = ObjStringConsumer.class.getMethod("accept", Object.class, String.class);
        ssetter = MethodReferenceFactory.methodReference(ObjStringConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToStringFunction.class.getMethod("apply", Object.class);
        sgetter = MethodReferenceFactory.methodReference(ToStringFunction.class, apply, getterHandle);
    }

    public String getString(Object parent)
    {
        return sgetter.apply(parent);
    }

    @Override
    public void set(String value, Object parent) throws AlkemyException
    {
        ssetter.accept(parent, value);
    }
}
