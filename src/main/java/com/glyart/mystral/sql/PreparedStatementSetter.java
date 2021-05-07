package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a callback interface used by {@link Database}.
 *
 * <p>Implementations of this interface set values on a {@link PreparedStatement} provided by the Database class.
 * They are responsible for setting parameters: a SQL statement with placeholders (question marks) will already have been supplied.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled internally by {@link DataOperations} implementations.
 * If the handling is done in async context then implementations should make the details available in some way.
 * {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    /**
     * Sets parameter values into the given active PreparedStatement.
     * @param ps the PreparedStatement to invoke setter methods on
     * @throws SQLException if an SQLException is encountered while trying to set values (no need to catch)
     */
    void setValues(@NotNull PreparedStatement ps) throws SQLException;
}
