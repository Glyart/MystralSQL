package com.glyart.mystral.data;

import com.glyart.mystral.exceptions.IncorrectDataSizeException;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class DataUtils {

    private DataUtils() {}

    /**
     * Return a single result object from the given Collection.
     * <p>Throws an exception if 0 or more than 1 element found.
     * @param results the result Collection (can be {@code null}
     * and is also expected to contain {@code null} elements)
     * @return the single result object
     * @throws IncorrectDataSizeException if the given collection doesn't contain exactly 1 element
     */
    @Nullable
    public static <T> T nullableSingleResult(@Nullable Collection<T> results) throws IncorrectDataSizeException {
        if (results == null || results.isEmpty()) {
            throw new IncorrectDataSizeException(1, 0);
        }
        if (results.size() > 1) {
            throw new IncorrectDataSizeException(1, results.size());
        }
        return results.iterator().next();
    }

    /**
     * Return a single result object from the given Collection.
     * <p>Throws an exception if more than 1 element found.
     * @param results the result Collection (can be {@code null}
     * and is also expected to contain {@code null} elements)
     * @return the single (nullable) result object
     * @throws IncorrectDataSizeException if the given collection contains more than 1 element
     */
    @Nullable
    public static <T> T nullableEmptyResult(@Nullable Collection<T> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectDataSizeException(1, results.size());
        }
        return results.iterator().next();
    }
}
