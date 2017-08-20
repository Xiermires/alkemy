package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjCharConsumer;
import org.alkemy.functional.ToCharFunction;
import org.alkemy.instr.AlkemizerUtils;
import org.alkemy.parse.ValueAccessor;

public class CharReference extends ObjectReference implements ValueAccessor
{
    private final ToCharFunction<Object> cgetter;
    private final ObjCharConsumer<Object> csetter;

    @SuppressWarnings("unchecked")
    public CharReference(Field f) throws NoSuchMethodException, SecurityException, IllegalAccessException
    {
        super(f);
        final String setterName = AlkemizerUtils.getSetterName(f.getName());
        final MethodHandle setterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), setterName, char.class);
        final Method accept = ObjCharConsumer.class.getMethod("accept", Object.class, char.class);
        csetter = MethodReferenceFactory.methodReference(ObjCharConsumer.class, accept, setterHandle);

        final String getterName = AlkemizerUtils.getGetterName(f.getName());
        final MethodHandle getterHandle = MethodReferenceFactory.methodHandle(f.getDeclaringClass(), getterName);
        final Method apply = ToCharFunction.class.getMethod("apply", Object.class);
        cgetter = MethodReferenceFactory.methodReference(ToCharFunction.class, apply, getterHandle);
    }

    public char getChar(Object parent)
    {
        return cgetter.apply(parent);
    }

    @Override
    public void set(char value, Object parent) throws AlkemyException
    {
        csetter.accept(parent, value);
    }
}
