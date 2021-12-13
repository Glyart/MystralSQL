package com.glyart.mystral.exceptions;

/**
 * Threw when trying to auto-mapping rows with the {@link com.glyart.mystral.data.ClassMapper} class.
 * Describes 2 possible wrong scenarios:
 * <ul>
 *     <li>There was an attempt to set a null value for a primitive type (which is impossible, because primitive types have their own non-null default values);</li>
 *     <li>An incompatible value was provided to the setter method</li>
 * </ul>
 */
public class TypeMismatchException extends RuntimeException {

    public TypeMismatchException(Throwable cause) {
        super(cause);
    }

    public TypeMismatchException(String message) {
        super(message);
    }

    public TypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
