package com.glyart.mystral.data;

import com.glyart.mystral.exceptions.ConversionException;

import java.util.function.Function;

/**
 * Represents a contract for data conversions.
 * Some types cannot be represented with SQL types (e.g. UUIDs. You can convert varchar to UUID, starting from their Java String representation).
 * With this interface, ClassMapper can invoke the setter methods of the objects to map, passing the correct values after they have been converted.
 * @param <T> the type to convert
 * @param <R> the desired type
 * @see com.glyart.mystral.data.support.StringToUUID
 */
@FunctionalInterface
public interface Converter<T, R> extends Function<T, R> {

    /**
     * Applies the conversion to the given value, transforming the object in a new type.
     * @param object the value to convert
     * @return the new converted type
     * @throws ConversionException if something went wrong
     */
    @Override
    R apply(T object) throws ConversionException;
}