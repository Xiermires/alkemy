package org.alkemy.parse.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alkemy.annotations.AlkemyLeaf;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@AlkemyLeaf
public @interface Bar
{
    long id() default 0;

    String desc() default "";
}