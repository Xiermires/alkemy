package org.alkemy.parse.impl;

import org.objectweb.asm.ClassVisitor;

public abstract class Alkemizer extends ClassVisitor
{
    public Alkemizer(int api, ClassVisitor cv)
    {
        super(api, cv);
    }

    abstract boolean isAlkemized();
}
