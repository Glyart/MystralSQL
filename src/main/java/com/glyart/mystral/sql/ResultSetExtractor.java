package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import com.glyart.mystral.sql.impl.DefaultExtractor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a callback interface used by {@link Database}'s query methods.
 *
 * <p>Implementations of this interface extract results from a {@link ResultSet} and they
 * don't need to worry about handling exceptions: they will be handled internally by {@link DataOperations} implementations.If the handling is done in async context then implementations should make the details available in some way. {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
 *
 * <p>This interface is internally used by Database and, like {@link ResultSetRowMapper}, it's reusable.
 * A default implementation called DefaultExtractor is already provided.</p>
 * @param <T> the result type
 * @see DefaultExtractor
 */
@FunctionalInterface
public interface ResultSetExtractor<T> {

    /**
     * Implementations of this method must provide the processing logic (data extraction) of the entire ResultSet.
     * @param resultSet the ResultSet to extract data from. Implementations don't need to close this: it will be closed by Database
     * @return an object result or null if it's not available
     * @throws SQLException if an SQLException is encountered while trying to navigate the ResultSet
     */
    @Nullable
    T extractData(@NotNull ResultSet resultSet) throws SQLException;
}
