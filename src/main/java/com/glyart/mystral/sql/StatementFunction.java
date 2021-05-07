package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Represents a callback interface for SQL statements.
 * It can execute multiple operations on a single Statement.
 * @param <T> The result type
 * @see Database#execute(StatementFunction)
 */
public interface StatementFunction<T>  {

    /**
     * Gets called by {@link Database#execute(StatementFunction)}.
     *
     * <p>Implementations don't need to worry about handling exceptions:
     * they will be handled internally by {@link DataOperations} implementations
     * If the handling is done in async context then implementations should make the details available in some way.
     * {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
     *
     * <p><b>ATTENTION: any ResultSet should be closed within this callback implementation. This method doesn't imply
     * that the ResultSet (as other resources) will be closed.
     * Still, this method should grant (as shown in Database various implementations) that the statement
     * will be closed at the end of the operations.</b></p>
     * @param statement an active statement
     * @return a result of the statement execution. Null if no results are available
     * @throws SQLException if a database error occured. If so, it will be properly handled by implementations
     * @see Database#update(String, boolean)
     * @see Database#query(String, ResultSetExtractor)
     */
    T apply(@NotNull Statement statement) throws SQLException;
}
