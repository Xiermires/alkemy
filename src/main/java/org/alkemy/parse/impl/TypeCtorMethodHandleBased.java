package org.alkemy.parse.impl;

import java.lang.invoke.MethodHandle;

import org.alkemy.exception.AlkemyException;

public class TypeCtorMethodHandleBased implements NodeConstructor
{
    private final Class<?> type;
    private final MethodHandle mh;

    public TypeCtorMethodHandleBased(Class<?> type, MethodHandle mh)
    {
        this.type = type;
        this.mh = mh;
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked") // safe
    public <T> T newInstance(Object... args) throws AlkemyException
    {
        try
        {
            return (T) mh.invokeWithArguments(args);
        }
        catch (Throwable e)
        {
            throw new AlkemyException("Invalid arguments.", e); // TODO.
        }
    }
}
