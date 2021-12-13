package com.glyart.mystral.exceptions;

/**
 * Represents an exception which describes a problem occurred while trying to convert a type to the desired one.
 */
public class ConversionException extends RuntimeException {

    public ConversionException(Throwable t) {
        this(t.getMessage(), t);
    }

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
