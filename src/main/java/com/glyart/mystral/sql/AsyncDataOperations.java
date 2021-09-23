package com.glyart.mystral.sql;

import com.glyart.mystral.database.AsyncDatabase;
import com.glyart.mystral.exceptions.DataAccessException;
import com.glyart.mystral.exceptions.IncorrectDataSizeException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Represents a basic set of asynchronous operations to interact with a data source.
 * All operations are done asynchronously by using {@link CompletableFuture} API.
 * <p>If you want to re-implement this interface consider to use the {@link Executor} interface.</p>
 * @see AsyncDatabase
 */
public interface AsyncDataOperations {

    /**
     * Executes a JDBC data access operation, implemented as {@link StatementFunction} callback, using an
     * active connection.
     * The callback CAN return a result object (if it exists), for example a single object or a collection of objects.
     * @param callback a callback that holds the operation logic
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<T> execute(@NotNull StatementFunction<T> callback) throws DataAccessException;

    /**
     * Performs a single update operation (like insert, delete, update).
     * @param sql static SQL statement to execute
     * @param getGeneratedKeys a boolean value
     * @return a never null CompletableFuture object which holds: the number of the affected rows.
     * <b>If getGeneratedKeys is true, this method will return the key of the new generated row</b>
     * @throws DataAccessException if there is any problem
     */
    CompletableFuture<Integer> update(@Language("MySQL") @NotNull String sql, boolean getGeneratedKeys) throws DataAccessException;

    /**
     * Executes a query given static SQL statement, then it reads the {@link ResultSet} using the {@link ResultSetExtractor} implementation.
     * @param sql the query to execute
     * @param extractor a callback that will extract all rows from the ResultSet
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, ResultSetExtractor<T> extractor) throws DataAccessException;

    /**
     * Executes a query given static SQL statement, then it reads the {@link ResultSet} using the {@link ResultSetExtractor} implementation.<br>
     * <p>If a {@code null} result is returned, then the given supplier will be invoked, supplying the specified result (it can be null) inside the CompletableFuture.</p>
     * @param sql the query to execute
     * @param extractor a callback that will extract all rows from the ResultSet
     * @param supplier the supplier of another result
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor (or the supplier) implementation,
     * otherwise the result of the supplier invocation
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<T> queryOrElseGet(@Language("MySQL") @NotNull String sql, ResultSetExtractor<T> extractor, @NotNull Supplier<T> supplier) throws DataAccessException;

    /**
     * Executes a query given static SQL statement, then it maps each
     * ResultSet row to a result object using the {@link ResultSetRowMapper} implementation.
     * @param sql the query to execute
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<List<T>> queryForList(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException;

    /**
     * Executes a query given static SQL statement, then it maps each
     * ResultSet row to a result object using the {@link ResultSetRowMapper} implementation.
     * <p>If a {@code null} collection is returned, then the given supplier will be invoked, supplying the specified list (it can be null) inside the CompletableFuture.</p>
     * @param sql the query to execute
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param supplier the supplier of another list
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist),
     * otherwise the result of the supplier invocation
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<List<T>> queryForListOrElseGet(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<List<T>> supplier);

    /**
     * Executes a query given static SQL statement, then it maps the first
     * ResultSet row to a result object using the {@link ResultSetRowMapper} implementation.
     * The ResultSet must have exactly ONE row.
     * @param sql the query to execute
     * @param resultSetRowMapper a callback that will map the object per ResultSet row
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a mapped result object (if it exists)
     * @throws DataAccessException if there is any problem
     * @throws IncorrectDataSizeException if the query doesn't return exactly one result
     */
    <T> CompletableFuture<T> queryForObject(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException;

    /**
     * Executes a query given static SQL statement, then it maps the first
     * ResultSet row to a result object using the {@link ResultSetRowMapper} implementation.<br>
     * The ResultSet mustn't have more than ONE row.
     * <p>If a {@code null} result is returned, then the given supplier will be invoked, supplying the specified result (it can be null) inside the CompletableFuture.</p>
     * @param sql the query to execute
     * @param resultSetRowMapper a callback that will map the object per ResultSet row
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a mapped result object (if it exists),
     * otherwise the result of the supplier invocation
     * @throws DataAccessException if there is any problem
     * @throws IncorrectDataSizeException if the query return more than one result
     */
    <T> CompletableFuture<T> queryForObjectOrElseGet(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<T> supplier) throws DataAccessException, IncorrectDataSizeException;

    /**
     * Executes a JDBC data access operation, implemented as {@link PreparedStatementFunction} callback
     * working on a PreparedStatement.
     * The callback CAN return a result object (if it exists), for example a singlet or a collection of objects.
     * @param creator a callback that creates a PreparedStatement object given a connection
     * @param callback a callback that holds the operation logic
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<T> execute(@NotNull PreparedStatementCreator creator, @NotNull PreparedStatementFunction<T> callback) throws DataAccessException;

    /**
     * Executes a JDBC data access operation, implemented as {@link PreparedStatementFunction} callback
     * working on a PreparedStatement.
     * The callback CAN return a result object (if it exists), for example a singlet or a collection of objects.
     * @param sql the SQL statement to execute
     * @param callback a callback that holds the operation logic
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<T> execute(@Language("MySQL") @NotNull String sql, @NotNull PreparedStatementFunction<T> callback) throws DataAccessException;
    
    /**
     * Performs a single update operation (like insert, delete, update) using a {@link PreparedStatementCreator} to provide SQL
     * and any required parameters. A {@link PreparedStatementSetter} can be passed as helper that sets bind parameters.
     * @param creator a callback that provides the PreparedStatement with bind parameters
     * @param setter a helper that sets bind parameters. If it's null then this will be an update with static SQL
     * @param getGeneratedKey a boolean value
     * @return a never null CompletableFuture object which holds: the number of the affected rows.
     * <b>If getGeneratedKeys is true, this method will return the key of the new generated row</b>
     * @throws DataAccessException if there is any problem
     */
    CompletableFuture<Integer> update(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) throws DataAccessException;

    /**
     * Performs a single update operation (like insert, delete, update) using a {@link PreparedStatementCreator} to provide SQL and any required parameters.
     * @param creator a callback that provides the PreparedStatement with required parameters
     * @param getGeneratedKeys a boolean values
     * @return a never null CompletableFuture object which holds: the number of the affected rows.
     * <b>If getGeneratedKeys is true, this method will return the key of the new generated row</b>
     * @throws DataAccessException if there is any problem
     */
    CompletableFuture<Integer> update(@NotNull PreparedStatementCreator creator, boolean getGeneratedKeys) throws DataAccessException;

    /**
     * Performs a single update operation (like insert, delete, update).
     * A {@link PreparedStatementSetter} can be passed as helper that sets bind parameters.
     * @param sql the SQL containing bind parameters
     * @param setter a helper that sets bind parameters. If it's null then this will be an update with static SQL
     * @param getGeneratedKey a boolean value
     * @return a never null CompletableFuture object which holds: the number of the affected rows.
     * <b>If getGeneratedKeys is true, this method will return the key of the new generated row</b>
     * @throws DataAccessException if there is any problem
     */
    CompletableFuture<Integer> update(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) throws DataAccessException;

    /**
     * Performs a single update operation (like insert, update or delete statement)
     * via PreparedStatement, binding the given parameters.
     * @param sql the SQL containing bind parameters
     * @param params arguments to be bind to the given SQL
     * @param getGeneratedKey a boolean value
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @return a never null CompletableFuture object which holds: the number of the affected rows.
     * <b>If getGeneratedKeys is true, this method will return the key of the new generated row</b>
     * @throws DataAccessException if there is any problem
     */
    CompletableFuture<Integer> update(@Language("MySQL") @NotNull String sql, Object[] params, boolean getGeneratedKey, int... sqlTypes) throws DataAccessException;

    /**
     * Performs multiple update operations using a single SQL statement.
     * <p><b>NOTE: this method will be unusable if the driver doesn't support batch updates.</b></p>
     * @param sql the SQL containing bind parameters. It will be
     * reused because all statements in a batch use the same SQL
     * @param batchSetter a callback that sets parameters on the PreparedStatement created by this method
     * @return a CompletableFuture object. It can be used to know when the batch update is done and if an exception occurred
     * @throws IllegalStateException if the driver doesn't support batch updates
     */
    CompletableFuture<Void> batchUpdate(@Language("MySQL") @NotNull String sql, @NotNull BatchSetter batchSetter) throws IllegalStateException;

    /**
     * Performs multiple update operations using a single SQL statement.
     * @param sql The SQL containing bind parameters. It will be
     * reused because all statements in a batch use the same SQL
     * @param batchArgs A list of object arrays containing the batch arguments
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @return A CompletableFuture object. It can be used for knowing when the batch update is done and if an exception occurred
     * @throws IllegalStateException If the driver doesn't support batch updates
     */
    CompletableFuture<Void> batchUpdate(@Language("MySQL") @NotNull String sql, @Nullable List<Object[]> batchArgs, int... sqlTypes) throws IllegalStateException;

    /**
     * Performs multiple update operations using a single SQL statement.
     * @param sql the SQL containing bind parameters. It will be
     * reused because all statements in a batch use the same SQL
     * @param batchArgs a list of objects containing the batch arguments
     * @param paramsBatchSetter a callback that sets parameters on the PreparedStatement created by this method
     * @param <T> the parameter type
     * @return a CompletableFuture object. It can be used for knowing when the batch update is done and if an exception occurred
     * @throws IllegalStateException if the driver doesn't support batch updates
     */
    <T> CompletableFuture<Void> batchUpdate(@Language("MySQL") @NotNull String sql, @Nullable List<T> batchArgs, @NotNull ParametrizedBatchSetter<T> paramsBatchSetter) throws IllegalStateException;

    /**
     * Executes a query using a PreparedStatement, created by a {@link PreparedStatementCreator} and with his values set
     * by a {@link PreparedStatementSetter}.
     *
     * <p>Most other query methods use this method, but application code will always
     * work with either a creator or a setter.</p>
     * @param creator a callback that creates a PreparedStatement
     * @param setter a callback that sets values on the PreparedStatement. If null, the SQL will be treated as static SQL with no bind parameters
     * @param extractor a callback that will extract results given a ResultSet
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @throws DataAccessException if there is any problem
     * @see PreparedStatementSetter
     */
    <T> CompletableFuture<T> query(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException;

    /**
     * Executes a query using a PreparedStatement, then reading the ResultSet with a {@link ResultSetExtractor} implementation.
     * @param creator a callback that creates a PreparedStatement
     * @param extractor a callback that will extract results given a ResultSet
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @throws DataAccessException if there is any problem
     * @see PreparedStatementCreator
     */
    <T> CompletableFuture<T> query(@NotNull PreparedStatementCreator creator, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException;

    /**
     * Executes a query using a PreparedStatement, mapping each row to a result object via a {@link ResultSetRowMapper} implementation.
     * @param psc a callback that creates a PreparedStatement
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<List<T>> query(@NotNull PreparedStatementCreator psc, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException;

    /**
     * Executes a query using an SQL statement, then reading the ResultSet with a {@link ResultSetExtractor} implementation.
     * @param sql the query to execute
     * @param setter a callback that sets values on the PreparedStatement. If null, the SQL will be treated as static SQL with no bind parameters
     * @param extractor a callback that will extract results given a ResultSet
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException;

    /**
     * Executes a query using an SQL statement and a {@link PreparedStatementSetter} implementation that will bind values to the query.
     * Each row of the ResultSet will be mapped to a result object via a ResultSetRowMapper implementation.
     * @param sql the query to execute
     * @param pss a callback that sets values on the PreparedStatement. If null, the SQL will be treated as static SQL with no bind parameters
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<List<T>> query(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter pss, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments will be bound to the query.
     * The {@link ResultSetExtractor} implementation will read the ResultSet.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param extractor a callback that will extract results given a ResultSet
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments with their sql {@link Types} will be bound to the query.
     * The {@link ResultSetExtractor} implementation will read the ResultSet.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param extractor a callback that will extract results given a ResultSet
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @throws DataAccessException if there is any problem
     * @see Types
     */
    <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor, int... sqlTypes) throws DataAccessException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments with their sql {@link Types} will be bound to the query.
     * The {@link ResultSetExtractor} implementation will read the ResultSet.<br>
     * <p>If a {@code null} result is returned, then the given supplier will be invoked, supplying the specified result (it can be null) inside the CompletableFuture.</p>
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param extractor a callback that will extract results given a ResultSet
     * @param supplier the supplier of another result
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation,
     * otherwise the result of the supplier invocation
     * @throws DataAccessException if there is any problem
     * @see Types
     */
    <T> CompletableFuture<T> queryOrElseGet(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor, Supplier<T> supplier, int... sqlTypes) throws DataAccessException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments will be bound to the query.
     * Each row of the ResultSet will be mapped to a result object via a {@link ResultSetRowMapper} implementation.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     * @throws DataAccessException if there is any problem
     */
    <T> CompletableFuture<List<T>> queryForList(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments with their sql {@link Types} will be bound to the query.
     * Each row of the ResultSet will be mapped to a result object via a {@link ResultSetRowMapper} implementation.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     * @throws DataAccessException if there is any problem
     * @see Types
     */
    <T> CompletableFuture<List<T>> queryForList(@Language("MySQL") @NotNull String sql, @Nullable Object[] args,
                                                @NotNull ResultSetRowMapper<T> resultSetRowMapper, int... sqlTypes) throws DataAccessException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments with their sql {@link Types} will be bound to the query.
     * Each row of the ResultSet will be mapped to a result object via a {@link ResultSetRowMapper} implementation.<br>
     * <b>If a {@code null} collection is returned, then the given supplier will be invoked, supplying the specified list (it can be null) inside the CompletableFuture.</b>
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param supplier the supplier of another list
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist),
     * otherwise the result of the supplier invocation
     * @throws DataAccessException if there is any problem
     * @see Types
     */
    <T> CompletableFuture<List<T>> queryForListOrElseGet(@Language("MySQL") @NotNull String sql, @Nullable Object[] args,
                                                         @NotNull ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<List<T>> supplier, int... sqlTypes);

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments will be bound to the query.
     * <p>The first ResultSet row will be mapped to a result object using the {@link ResultSetRowMapper} implementation.</p>
     * The ResultSet must have exactly ONE row.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a mapped result object (if it exists)
     * @throws DataAccessException if there is any problem
     * @throws IncorrectDataSizeException if the query doesn't return exactly one result
     */
    <T> CompletableFuture<T> queryForObject(@Language("MySQL") @NotNull String sql, Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException, IncorrectDataSizeException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments with their sql {@link Types} will be bound to the query.
     * <p>The first ResultSet row will be mapped to a result object using the {@link ResultSetRowMapper} implementation.</p>
     * The ResultSet must have exactly ONE row.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a mapped result object (if it exists)
     * @throws DataAccessException if there is any problem
     * @see Types
     */
    <T> CompletableFuture<T> queryForObject(@Language("MySQL") @NotNull String sql, Object[] args, ResultSetRowMapper<T> resultSetRowMapper, int... sqlTypes) throws DataAccessException;

    /**
     * Executes a query given an SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments with their sql {@link Types} will be bound to the query.
     * <p>The first ResultSet row will be mapped to a result object using the {@link ResultSetRowMapper} implementation.</p>
     * The ResultSet mustn't have more ONE row.
     * <p>If a {@code null} result is returned, then the given supplier will be invoked, supplying the specified result (it can be null).</p>
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param resultSetRowMapper a callback that will map one object per ResultSet row
     * @param supplier the supplier of another result
     * @param sqlTypes an integer array containing the type of the query's parameters, expressed as {@link java.sql.Types}
     * @param <T> the result type
     * @return a never null CompletableFuture object which holds: a mapped result object (if it exists),
     * otherwise the result of the supplier invocation
     * @throws DataAccessException if there is any problem
     * @throws IncorrectDataSizeException if the query return more than one result
     * @see Types
     */
    <T> CompletableFuture<T> queryForObjectOrElseGet(@Language("MySQL") @NotNull String sql, Object[] args, ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<T> supplier, int... sqlTypes) throws DataAccessException, IncorrectDataSizeException;
}
