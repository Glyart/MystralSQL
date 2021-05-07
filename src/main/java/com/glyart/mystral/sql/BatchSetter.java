package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a batch update callback interface used by the {@link Database} class.
 *
 * <p>Implementations set values on a {@link PreparedStatement} provided by The Database class, for each
 * of a number of updates in a batch using the same SQL statement.
 * They are responsible for setting parameters: a SQL statement with placeholders (question marks) will already have been supplied.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled internally by {@link DataOperations} implementations.
 * If the handling is done in async context then implementations should make the details available in some way. 
 * {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
 */
public interface BatchSetter {

    /**
     * Sets parameter values on the given PreparedStatement.
     * @param preparedStatement an active PreparedStatement for invoking setter methods
     * @param i index of the statement inside the batch, starting from 0
     * @throws SQLException if an SQLException is encountered while trying to set values (no need to catch)
     */
    void setValues(@NotNull PreparedStatement preparedStatement, int i) throws SQLException;

    /**
     * Gets the size of the batch.
     * @return the number of statements in the batch
     */
    int getBatchSize();
}
