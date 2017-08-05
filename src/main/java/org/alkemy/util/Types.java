package org.alkemy.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class Types
{
    private Types() {
    }

    /**
     * <ul>
     * <li>If the type is an array. Equivalent to {@link Class#getComponentType()}
     * <li>If the type is a collection, returns the collection's defined generic type.
     * <li>Otherwise returns null.
     * </ul>
     */
    public static Class<?> getComponentType(Field f)
    {
        if (f.getType().isArray())
        {
            return f.getType().getComponentType();
        }
        else if (Collection.class.isAssignableFrom(f.getType()))
        {
            final Type genericType = f.getGenericType();
            if (genericType instanceof ParameterizedType)
            {
                final Type[] genericTypes = ((ParameterizedType) genericType).getActualTypeArguments();
                try
                {
                    return Class.forName(genericTypes[0].getTypeName());
                }
                catch (Exception e)
                {
                    return null;
                }
            }
        }
        return null;
    }
}
