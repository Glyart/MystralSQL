package com.glyart.mystral.sql;

import com.glyart.mystral.database.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a callback interface for code that operates on a PreparedStatement.
 * This is internally used by the {@link Database} class but it's also useful for custom purposes.
 *
 * <p>Note: the passed-in PreparedStatement may have been created by {@link Database} or by a custom {@link PreparedStatementCreator} implementation.
 * However, the latter is hardly ever necessary, as most custom callback actions will perform updates
 * in which case a standard PreparedStatement is fine. Custom actions will
 * always set parameter values themselves, so that PreparedStatementCreator
 * capability is not needed either.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled internally by {@link DataOperations} implementations.
 * If the handling is done in async context then implementations should make the details available in some way.
 * {@link AsyncDataOperations} may place these details inside a never null CompletableFuture object.</p>
 * @param <T> the result type
 * @see Database#execute(String, PreparedStatementFunction)
 * @see Database#execute(PreparedStatementCreator, PreparedStatementFunction)
 */
@FunctionalInterface
public interface PreparedStatementFunction<T> {

    /**
     * Gets called by {@link Database#execute(String, PreparedStatementFunction)}
     * or {@link Database#execute(PreparedStatementCreator, PreparedStatementFunction)}.
     * <p><b>ATTENTION: any ResultSet should be closed within this callback implementation.
     * This method doesn't imply that the ResultSet (as other resources) will be closed.
     * Still, this method should grant (as shown in Database various implementations) that the statement will be closed at the end of the operations.</b>
     * @param ps an active PreparedStatement
     * @return a result object or null if it's not available
     * @throws SQLException if thrown by a Database's method (no need to catch).
     */
    @Nullable
    T apply(@NotNull PreparedStatement ps) throws SQLException;
}
