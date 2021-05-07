package com.glyart.mystral.sql;

import com.glyart.mystral.exceptions.DataAccessException;
import org.jetbrains.annotations.Nullable;

/**
 *<p>Represents an interface to be implemented by objects which can provide SQL strings.</p>
 *
 * <p>This interface is implemented alongside {@link StatementFunction}s and {@link PreparedStatementFunction}s.
 * This helps retrieving better contextual information in case of {@link DataAccessException} exceptions.</p>
 * <p>Use of this interface is suggested in application code (if possible).</p>
 */
@FunctionalInterface
public interface SqlProvider {

    /**
     * Returns the sql statement related to this implementation.
     * @return the sql (if available)
     */
    @Nullable
    String getSql();
}
