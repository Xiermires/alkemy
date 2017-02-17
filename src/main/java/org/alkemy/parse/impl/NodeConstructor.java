package org.alkemy.parse.impl;

import org.alkemy.exception.AccessException;
import org.alkemy.exception.AlkemyException;

public interface NodeConstructor
{
    /**
     * Returns the class type.
     * 
     * @throws AccessException
     *             If an error occurs while recovering the class type.
     */
    Class<?> type() throws AlkemyException;
    
    /**
     * Returns a new instance of the class.
     * 
     * @throws AlkemyException
     *             If an error occurs while creating the class instance.
     */
    <T> T newInstance(Object... args) throws AlkemyException;
}
