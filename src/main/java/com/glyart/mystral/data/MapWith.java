package com.glyart.mystral.data;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation specify what converter should be used before passing a value to a setter method.
 * @see Converter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface MapWith {

    /**
     * Returns the converter class that will convert some value's type into the desired one.
     * @return the converter class
     */
    @NotNull
    Class<? extends Converter<?,?>> value();
}
