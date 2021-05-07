package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Represents a callback interface used by the {@link Database} class for executing batch updates.
 *
 * <p>Implementations of this interface set values on a {@link PreparedStatement} provided by the Database class, for each
 * of a number of updates in a batch using the same SQL statement.
 * They are responsible for setting parameters: a SQL statement with placeholders (question marks) will already have been supplied.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled internally by {@link DataOperations} implementations.
 * If the handling is done in async context then implementations should make the details available in some way. 
 * {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
 * @param <T> yhe argument type
 * @see Database#batchUpdate(String, List, ParametrizedBatchSetter)
 */
@FunctionalInterface
public interface ParametrizedBatchSetter<T> {

    /**
     * Sets the parameter values of the T argument inside the PreparedStatement
     * @param preparedStatement an active PreparedStatement
     * @param argument a generic object containing the values to set
     * @throws SQLException if an SQLException is encountered while trying to set values (no need to catch)
     */
    void setValues(@NotNull PreparedStatement preparedStatement, T argument) throws SQLException;
}
