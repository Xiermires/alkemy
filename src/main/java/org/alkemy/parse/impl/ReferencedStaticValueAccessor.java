/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.alkemy.parse.impl;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjBooleanConsumer;
import org.alkemy.functional.ObjByteConsumer;
import org.alkemy.functional.ObjCharConsumer;
import org.alkemy.functional.ObjDoubleConsumer;
import org.alkemy.functional.ObjFloatConsumer;
import org.alkemy.functional.ObjIntConsumer;
import org.alkemy.functional.ObjLongConsumer;
import org.alkemy.functional.ObjShortConsumer;
import org.alkemy.functional.ObjStringConsumer;
import org.alkemy.functional.ToBooleanFunction;
import org.alkemy.functional.ToByteFunction;
import org.alkemy.functional.ToCharFunction;
import org.alkemy.functional.ToDoubleFunction;
import org.alkemy.functional.ToFloatFunction;
import org.alkemy.functional.ToIntFunction;
import org.alkemy.functional.ToLongFunction;
import org.alkemy.functional.ToShortFunction;
import org.alkemy.functional.ToStringFunction;
import org.alkemy.parse.ValueAccessor;

public class ReferencedStaticValueAccessor implements ValueAccessor
{
    protected final String name;
    protected final Class<?> type;

    // All types.
    protected final Function<Object, ?> getter;
    protected final BiConsumer<Object, Object> setter;

    // Primitives + String.
    protected ToStringFunction<Object> stringGetter;
    protected ObjStringConsumer<Object> stringSetter;

    protected ToDoubleFunction<Object> doubleGetter;
    protected ObjDoubleConsumer<Object> doubleSetter;

    protected ToFloatFunction<Object> floatGetter;
    protected ObjFloatConsumer<Object> floatSetter;

    protected ToLongFunction<Object> longGetter;
    protected ObjLongConsumer<Object> longSetter;

    protected ToIntFunction<Object> intGetter;
    protected ObjIntConsumer<Object> intSetter;

    protected ToShortFunction<Object> shortGetter;
    protected ObjShortConsumer<Object> shortSetter;

    protected ToCharFunction<Object> charGetter;
    protected ObjCharConsumer<Object> charSetter;

    protected ToByteFunction<Object> byteGetter;
    protected ObjByteConsumer<Object> byteSetter;

    protected ToBooleanFunction<Object> booleanGetter;
    protected ObjBooleanConsumer<Object> booleanSetter;

    protected ReferencedStaticValueAccessor(String name, Class<?> type, Function<Object, ?> getter, BiConsumer<Object, Object> setter)
    {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    ReferencedStaticValueAccessor stringSetter(ObjStringConsumer<Object> stringSetter)
    {
        this.stringSetter = stringSetter;
        return this;
    }
    
    ReferencedStaticValueAccessor stringGetter(ToStringFunction<Object> stringGetter)
    {
        this.stringGetter = stringGetter;
        return this;
    }

    ReferencedStaticValueAccessor doubleSetter(ObjDoubleConsumer<Object> doubleSetter)
    {
        this.doubleSetter = doubleSetter;
        return this;
    }

    ReferencedStaticValueAccessor doubleGetter(ToDoubleFunction<Object> doubleGetter)
    {
        this.doubleGetter = doubleGetter;
        return this;
    }

    ReferencedStaticValueAccessor floatSetter(ObjFloatConsumer<Object> floatSetter)
    {
        this.floatSetter = floatSetter;
        return this;
    }

    ReferencedStaticValueAccessor floatGetter(ToFloatFunction<Object> floatGetter)
    {
        this.floatGetter = floatGetter;
        return this;
    }

    ReferencedStaticValueAccessor longSetter(ObjLongConsumer<Object> longSetter)
    {
        this.longSetter = longSetter;
        return this;
    }

    ReferencedStaticValueAccessor longGetter(ToLongFunction<Object> longGetter)
    {
        this.longGetter = longGetter;
        return this;
    }

    ReferencedStaticValueAccessor intSetter(ObjIntConsumer<Object> intSetter)
    {
        this.intSetter = intSetter;
        return this;
    }

    ReferencedStaticValueAccessor intGetter(ToIntFunction<Object> intGetter)
    {
        this.intGetter = intGetter;
        return this;
    }

    ReferencedStaticValueAccessor shortSetter(ObjShortConsumer<Object> shortSetter)
    {
        this.shortSetter = shortSetter;
        return this;
    }

    ReferencedStaticValueAccessor shortGetter(ToShortFunction<Object> shortGetter)
    {
        this.shortGetter = shortGetter;
        return this;
    }

    ReferencedStaticValueAccessor charSetter(ObjCharConsumer<Object> charSetter)
    {
        this.charSetter = charSetter;
        return this;
    }

    ReferencedStaticValueAccessor charGetter(ToCharFunction<Object> charGetter)
    {
        this.charGetter = charGetter;
        return this;
    }

    ReferencedStaticValueAccessor byteSetter(ObjByteConsumer<Object> byteSetter)
    {
        this.byteSetter = byteSetter;
        return this;
    }

    ReferencedStaticValueAccessor byteGetter(ToByteFunction<Object> byteGetter)
    {
        this.byteGetter = byteGetter;
        return this;
    }

    ReferencedStaticValueAccessor booleanSetter(ObjBooleanConsumer<Object> booleanSetter)
    {
        this.booleanSetter = booleanSetter;
        return this;
    }

    ReferencedStaticValueAccessor booleanGetter(ToBooleanFunction<Object> booleanGetter)
    {
        this.booleanGetter = booleanGetter;
        return this;
    }

    @Override
    public Class<?> type()
    {
        return type;
    }

    @Override
    public Object get(Object parent) throws AlkemyException
    {
        return getter.apply(parent);
    }

    @Override
    public void set(Object value, Object parent) throws AlkemyException
    {
        setter.accept(parent, value);
    }

    @Override
    public String valueName()
    {
        return name;
    }

    @Override
    public void set(String value, Object parent) throws AlkemyException
    {
        if (stringSetter != null)
            stringSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public double getDouble(Object parent) throws AlkemyException
    {
        return doubleGetter != null ? doubleGetter.apply(parent) : get(parent, Double.class);
    }

    @Override
    public void set(double value, Object parent) throws AlkemyException
    {
        if (doubleSetter != null)
            doubleSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public float getFloat(Object parent) throws AlkemyException
    {
        return floatGetter != null ? floatGetter.apply(parent) : get(parent, Float.class);
    }

    @Override
    public void set(float value, Object parent) throws AlkemyException
    {
        if (floatSetter != null)
            floatSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public long getLong(Object parent) throws AlkemyException
    {
        return longGetter != null ? longGetter.apply(parent) : get(parent, Long.class);
    }

    @Override
    public void set(long value, Object parent) throws AlkemyException
    {
        if (longSetter != null)
            longSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public int getInt(Object parent) throws AlkemyException
    {
        return intGetter != null ? intGetter.apply(parent) : get(parent, Integer.class);
    }

    @Override
    public void set(int value, Object parent) throws AlkemyException
    {
        if (intSetter != null)
            intSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public short getShort(Object parent) throws AlkemyException
    {
        return shortGetter != null ? shortGetter.apply(parent) : get(parent, Short.class);
    }

    @Override
    public void set(short value, Object parent) throws AlkemyException
    {
        if (shortSetter != null)
            shortSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public char getChar(Object parent) throws AlkemyException
    {
        return charGetter != null ? charGetter.apply(parent) : get(parent, Character.class);
    }

    @Override
    public void set(char value, Object parent) throws AlkemyException
    {
        if (charSetter != null)
            charSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public byte getByte(Object parent) throws AlkemyException
    {
        return byteGetter != null ? byteGetter.apply(parent) : get(parent, Byte.class);
    }

    @Override
    public void set(byte value, Object parent) throws AlkemyException
    {
        if (byteSetter != null)
            byteSetter.accept(parent, value);
        else setter.accept(parent, value);
    }

    @Override
    public boolean getBoolean(Object parent) throws AlkemyException
    {
        return booleanGetter != null ? booleanGetter.apply(parent) : get(parent, Boolean.class);
    }

    @Override
    public void set(boolean value, Object parent) throws AlkemyException
    {
        if (booleanSetter != null)
            booleanSetter.accept(parent, value);
        else setter.accept(parent, value);
    }
}
