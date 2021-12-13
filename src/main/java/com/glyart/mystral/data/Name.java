package com.glyart.mystral.data;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class fields and column names may differ from names.
 * To help mappings, this annotation links class fields which names are different from the table's ones.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Name {

    /**
     * Represents the column name to link to the annotated setter.
     * @return the column name
     */
    @NotNull
    String columnName();
}
