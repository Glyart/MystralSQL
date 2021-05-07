package com.glyart.mystral.exceptions;

import com.glyart.mystral.sql.AsyncDataOperations;
import com.glyart.mystral.sql.DataOperations;
import com.glyart.mystral.sql.SqlProvider;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

/**
 * Represents an exception that occurs when there is a problem during an operation issued by
 * implementations of {@link DataOperations} or {@link AsyncDataOperations}.
 *
 * <p>Using {@link SqlProvider} is highly suggested. This allows the exception to communicate the sql that issued the problem.</p>
 */
public class DataAccessException extends RuntimeException {

    @Nullable
    private String sql;

    public DataAccessException(String msg) {
        super(msg);
    }

    public DataAccessException(String msg, SQLException e) {
        this(msg, null, e);
    }

    public DataAccessException(String msg, @Nullable String sql, SQLException e) {
        super(String.format("Custom Message: %s - SQL: %s - Exception message: %s (%d)", msg, (sql == null ? "" : sql), e.getMessage(), e.getErrorCode()), e);
        this.sql = sql;
    }

    /**
     * Gets the sql statement that provoked this exception.
     * @return the sql (if it is available)
     */
    @Nullable
    public String getSql() {
        return this.sql;
    }
}
