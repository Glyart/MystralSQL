package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents an interface used to map rows of a {@link ResultSet}.
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled internally by {@link DataOperations} implementations
 * If the handling is done in async context then implementations should make the details available in some way.
 * {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
 *
 * <p>This interface can be used for query methods or for other custom implementations.</p><br>
 * A RowMapper object is reusable. Also, it's a convenient and fast way for implementing a row-mapping logic in a single spot.
 * @param <T> the result type
 * @see Database#queryForList(String, ResultSetRowMapper)
 */
@FunctionalInterface
public interface ResultSetRowMapper<T> {

    /**
     * Implementations will tell how to map EACH row of the ResultSet.<br>
     * {@link ResultSet#next()} call is not needed: this method should only map values of the current row.
     * @param resultSet the ResultSet, already initialized
     * @param rowNumber the number of the current row
     * @return a result object for the current row, or null if the result is not available
     * @throws SQLException if the implementation's trying to get column values in the wrong way
     */
    @Nullable
    T map(@NotNull ResultSet resultSet, int rowNumber) throws SQLException;
}
