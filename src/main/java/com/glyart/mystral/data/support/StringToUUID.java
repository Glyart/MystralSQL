package com.glyart.mystral.data.support;

import com.glyart.mystral.data.Converter;
import com.glyart.mystral.exceptions.ConversionException;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A converter which converts string to UUIDs.
 */
public class StringToUUID implements Converter<String, UUID> {

    /**
     * Converts a string to its UUID representation via {@link UUID#fromString(String)}.
     * @param object the string object to convert to UUID
     * @return a UUID from the specified string object
     * @throws ConversionException if something went wrong while trying to perform the convertion
     * @throws IllegalArgumentException if the parameter does not conform to the string representation as described in toString
     * @see UUID#fromString(String) 
     */
    @Override
    @NotNull
    public UUID apply(@NotNull String object) throws ConversionException, IllegalArgumentException {
        try {
            return UUID.fromString(Preconditions.checkNotNull(object));
        } catch (IllegalArgumentException e) {
            throw new ConversionException(e);
        }
    }
}
