/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
package org.alkemy.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.alkemy.AbstractAlkemyElement;
import org.alkemy.AbstractAlkemyElement.AlkemyElement;
import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.common.LabelledElementVisitor.LabelledElement;
import org.alkemy.visitor.AlkemyElementVisitor;

public class LabelledElementVisitor implements AlkemyElementVisitor<LabelledElement>
{
    private final Pattern p;
    private final BiFunction<String, Object, Object> f;
    private Map<String, String> dynamicVariables = new HashMap<>();

    public LabelledElementVisitor(BiFunction<String, Object, Object> f)
    {
        this(f, "\\{&(.+?)\\}");
    }

    public LabelledElementVisitor(BiFunction<String, Object, Object> f, String dynParamPattern)
    {
        this.f = f;
        this.p = Pattern.compile(dynParamPattern);
    }

    public LabelledElementVisitor dynamicVariables(Map<String, String> dynamicVariables)
    {
        this.dynamicVariables.putAll(dynamicVariables);
        return this;
    }
    
    @Override
    public void visit(LabelledElement e, Object parent)
    {
        final LabelledElement le = e;
        f.apply(DynamicLabel.replace(le.raw, dynamicVariables, p), le.get(parent));
    }

    @Override
    public LabelledElement map(AlkemyElement e)
    {
        return new LabelledElement(e);
    }

    static class LabelledElement extends AbstractAlkemyElement<LabelledElement>
    {
        String raw;

        protected LabelledElement(AbstractAlkemyElement<?> ae)
        {
            super(ae);
            raw = ae.desc().getAnnotation(Label.class).value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf(LabelledElementVisitor.class)
    public @interface Label
    {
        String value() default "";
    }
}
