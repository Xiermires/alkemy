package org.alkemy.parse.impl;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.alkemy.exception.AlkemyException;
import org.alkemy.functional.ObjBooleanConsumer;
import org.alkemy.functional.ObjByteConsumer;
import org.alkemy.functional.ObjCharConsumer;
import org.alkemy.functional.ObjFloatConsumer;
import org.alkemy.functional.ObjShortConsumer;
import org.alkemy.functional.ToByteFunction;
import org.alkemy.functional.ToCharFunction;
import org.alkemy.functional.ToFloatFunction;
import org.alkemy.functional.ToShortFunction;

public class StaticInstrumentAccessor implements ValueAccessor
{
    protected final String name;
    protected final Class<?> type;

    // Untypified.
    protected final Function<Object, ?> getter;
    protected final BiConsumer<Object, Object> setter;

    // Typified.
    protected Function<Object, String> stringGetter;
    protected BiConsumer<Object, String> stringSetter;

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

    protected Predicate<Object> booleanGetter;
    protected ObjBooleanConsumer<Object> booleanSetter;

    protected StaticInstrumentAccessor(String name, Class<?> type, Function<Object, ?> getter, BiConsumer<Object, Object> setter)
    {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    StaticInstrumentAccessor stringSetter(BiConsumer<Object, String> stringSetter)
    {
        this.stringSetter = stringSetter;
        return this;
    }

    StaticInstrumentAccessor doubleSetter(ObjDoubleConsumer<Object> doubleSetter)
    {
        this.doubleSetter = doubleSetter;
        return this;
    }

    StaticInstrumentAccessor doubleGetter(ToDoubleFunction<Object> doubleGetter)
    {
        this.doubleGetter = doubleGetter;
        return this;
    }

    StaticInstrumentAccessor floatSetter(ObjFloatConsumer<Object> floatSetter)
    {
        this.floatSetter = floatSetter;
        return this;
    }

    StaticInstrumentAccessor floatGetter(ToFloatFunction<Object> floatGetter)
    {
        this.floatGetter = floatGetter;
        return this;
    }

    StaticInstrumentAccessor longSetter(ObjLongConsumer<Object> longSetter)
    {
        this.longSetter = longSetter;
        return this;
    }

    StaticInstrumentAccessor longGetter(ToLongFunction<Object> longGetter)
    {
        this.longGetter = longGetter;
        return this;
    }

    StaticInstrumentAccessor intSetter(ObjIntConsumer<Object> intSetter)
    {
        this.intSetter = intSetter;
        return this;
    }

    StaticInstrumentAccessor intGetter(ToIntFunction<Object> intGetter)
    {
        this.intGetter = intGetter;
        return this;
    }

    StaticInstrumentAccessor shortSetter(ObjShortConsumer<Object> shortSetter)
    {
        this.shortSetter = shortSetter;
        return this;
    }

    StaticInstrumentAccessor shortGetter(ToShortFunction<Object> shortGetter)
    {
        this.shortGetter = shortGetter;
        return this;
    }

    StaticInstrumentAccessor charSetter(ObjCharConsumer<Object> charSetter)
    {
        this.charSetter = charSetter;
        return this;
    }

    StaticInstrumentAccessor charGetter(ToCharFunction<Object> charGetter)
    {
        this.charGetter = charGetter;
        return this;
    }

    StaticInstrumentAccessor byteSetter(ObjByteConsumer<Object> byteSetter)
    {
        this.byteSetter = byteSetter;
        return this;
    }

    StaticInstrumentAccessor byteGetter(ToByteFunction<Object> byteGetter)
    {
        this.byteGetter = byteGetter;
        return this;
    }

    StaticInstrumentAccessor booleanSetter(ObjBooleanConsumer<Object> booleanSetter)
    {
        this.booleanSetter = booleanSetter;
        return this;
    }

    StaticInstrumentAccessor booleanGetter(Predicate<Object> booleanGetter)
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
        return doubleGetter != null ? doubleGetter.applyAsDouble(parent) : get(parent, Double.class);
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
        return longGetter != null ? longGetter.applyAsLong(parent) : get(parent, Long.class);
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
        return intGetter != null ? intGetter.applyAsInt(parent) : get(parent, Integer.class);
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
        return booleanGetter != null ? booleanGetter.test(parent) : get(parent, Boolean.class);
    }

    @Override
    public void set(boolean value, Object parent) throws AlkemyException
    {
        if (booleanSetter != null)
            booleanSetter.accept(parent, value);
        else setter.accept(parent, value);
    }
}
