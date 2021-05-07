package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
/**
 * Represents a callback interface used by multiple methods of the {@link Database} class.
 *
 * <p>Implementations create a PreparedStatement with a given active Connection, provided by the Database class. Still, they
 * are responsible for providing the SQL statement and any necessary parameters.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled internally by {@link DataOperations} implementations.
 * If the handling is done in async context then implementations should make the details available in some way. 
 * {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
 */
@FunctionalInterface
public interface PreparedStatementCreator {
    
    /**
     * Creates a PreparedStatement in this connection. There is no need to close the PreparedStatement:
     * the Database class will do that.
     * @param connection the connection for creating the PreparedStatement
     * @return a PreparedStatement object
     * @throws SQLException if something goes wrong during the PreparedStatement creation (no need to catch)
     */
    @NotNull
    PreparedStatement create(@NotNull Connection connection) throws SQLException;
}
