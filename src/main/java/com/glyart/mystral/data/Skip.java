package com.glyart.mystral.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the field linked to this setter should be excluded from the automatic mapping.
 * A default value, depending on the field type, will be used instead.
 * <ul>
 *     <li>Object: null</li>
 *     <li>Primitive type: primitive type default value</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Skip { }
