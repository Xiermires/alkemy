package org.alkemy.parse.impl;

import org.alkemy.exception.AlkemyException;

public class StaticMethodLambdaBasedConstructor implements NodeConstructor
{
    private final Class<?> type;
    private final NodeConstructorFunction ctor;

    StaticMethodLambdaBasedConstructor(Class<?> type, NodeConstructorFunction ctor)
    {
        this.type = type;
        this.ctor = ctor;
    }

    @Override
    public Class<?> type() throws AlkemyException
    {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked") // returns an instance of type()
    public <T> T newInstance(Object... args) throws AlkemyException
    {
        try
        {
            return (T) ctor.newInstance(args);
        }
        catch (Throwable e)
        {
            throw new AlkemyException("Invalid arguments.", e); // TODO.
        }
    }
}
