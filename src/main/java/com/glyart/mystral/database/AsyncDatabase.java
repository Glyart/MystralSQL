package com.glyart.mystral.database;

import com.glyart.mystral.datasource.DataSourceSupplier;
import com.glyart.mystral.exceptions.DataAccessException;
import com.glyart.mystral.sql.*;
import com.google.common.base.Preconditions;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Represents implementation of basic asynchronous operations for interacting with a data source.
 * <p>An instance of {@code AsyncDatabase} is available by passing a valid {@link DataSource} and an implementation of
 * the {@link Executor} interface.</p>
 *
 * <p>This class:
 * <ul>
 *     <li>works asynchronously, without overhead on the main thread. This is done by using {@link Executor#execute(Runnable)}</li>
 *     <li>executes all the <a href="https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">CRUD</a> operations on a data source</li>
 *     <li>handles exceptions and communicates the sql that generated the problem (if possible)</li>
 *     <li>gives not null <b>{@link CompletableFuture} objects that WILL STORE usable future results</b></li>
 *     <li>iterates over {@link java.sql.ResultSet}s</li>
 *     <li>deals with static and prepared statements</li>
 * </ul>
 *
 * <p>Methods of this class use various callback interfaces. A reading of those is greatly suggested.
 *
 * <br> Since the callback interfaces make AsyncDatabase's methods parameterizable, there should be no need to subclass this class.</p>
 * @see Executor
 * @see CompletableFuture
 * @see BatchSetter
 * @see ParametrizedBatchSetter
 * @see PreparedStatementFunction
 * @see PreparedStatementCreator
 * @see ResultSetExtractor
 * @see ResultSetRowMapper
 * @see StatementFunction
 */
public class AsyncDatabase implements AsyncDataOperations {

    protected final DataOperations operations;
    protected final Executor executor;

    /**
     * Constructs a new {@link AsyncDatabase} instance by wrapping the given {@link DataSource} object in the default {@link Database}
     * implementation.
     * @param dataSource a valid DataSource instance
     * @param executor an implementation of {@link Executor} used to create new threads for executing the asynchronous JDBC operations.
     * @see Executor
     */
    public AsyncDatabase(@NotNull DataSource dataSource, @NotNull Executor executor) {
        this(new Database(dataSource), executor);
    }

    /**
     * Constructs a new {@link AsyncDatabase} that will use the given {@link DataOperations}'s implementations asynchronously via {@link Executor}.
     * @param operations the implementation of JDBC operations
     * @param executor an implementation of {@link Executor} used to create new threads for executing the asynchronous JDBC operations.
     * @see Executor
     */
    public AsyncDatabase(@NotNull DataOperations operations, @NotNull Executor executor) {
        Preconditions.checkNotNull(operations, "DataOperations cannot be null.");
        Preconditions.checkNotNull(executor, "The Executor cannot be null");
        this.operations = operations;
        this.executor = executor;
    }

    /**
     * Returns the Database class instance which wraps the given DataSource's instance.<br>
     * This method can return an empty Optional if the {@link #AsyncDatabase(DataOperations, Executor)}
     * constructor is used to create a new instance of this class.
     * @return the {@link Database} (it can be used for synchronous operations).
     * @throws IllegalStateException if there is no {@link Database} available
     * @see Optional
     */
    public Optional<Database> getDatabase() {
        return Optional.ofNullable(operations instanceof Database ? (Database) operations : null);
    }

    /**
     * Returns an Optional instance containing the {@link DataSource} used by this {@link AsyncDatabase}'s instance.
     * The AsyncDatabase class can obtain the DataSource from the {@link DataOperations} implementation.<br>
     * If that implementation is neither an instance of {@link DataSourceSupplier} nor {@link DatabaseAccessor} then an empty
     * Optional will be returned.
     * @return the DataSource
     * @throws IllegalStateException if there was a problem while trying to retrieve the {@link DataSource}'s instance
     * @see Optional
     */
    public Optional<DataSource> getDataSource() {
        if (operations instanceof DataSourceSupplier)
            return Optional.of(((DataSourceSupplier) operations).get());

        return Optional.ofNullable(operations instanceof DatabaseAccessor ? ((DatabaseAccessor) operations).getDataSource() : null);
    }

    @Override
    public <T> CompletableFuture<T> execute(@NotNull StatementFunction<T> callback) throws DataAccessException {
        return supplyAsync(() -> operations.execute(callback), executor);
    }

    @Override
    public CompletableFuture<Integer> update(@Language("MySQL") @NotNull String sql, boolean getGeneratedKeys) throws DataAccessException {
        return supplyAsync(() -> operations.update(sql, getGeneratedKeys), executor);
    }

    @Override
    public <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, ResultSetExtractor<T> extractor) throws DataAccessException {
        return supplyAsync(() -> operations.query(sql, extractor), executor);
    }

    @Override
    public <T> CompletableFuture<T> queryOrElseGet(@NotNull String sql, ResultSetExtractor<T> extractor, @NotNull Supplier<T> supplier) {
        return supplyAsync(() -> operations.queryOrElseGet(sql, extractor, supplier), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> queryForList(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException {
        return supplyAsync(() -> operations.queryForList(sql, resultSetRowMapper), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> queryForListOrElseGet(@NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<List<T>> supplier) {
        return supplyAsync(() -> operations.queryForListOrElseGet(sql, resultSetRowMapper, supplier));
    }

    @Override
    public <T> CompletableFuture<T> queryForObject(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException {
        return supplyAsync(() -> operations.queryForObject(sql, resultSetRowMapper), executor);
    }

    @Override
    public <T> CompletableFuture<T> queryForObjectOrElseGet(@NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<T> supplier) {
        return supplyAsync(() -> operations.queryForObjectOrElseGet(sql, resultSetRowMapper, supplier), executor);
    }

    @Override
    public <T> CompletableFuture<T> execute(@NotNull PreparedStatementCreator creator, @NotNull PreparedStatementFunction<T> callback) throws DataAccessException {
        return supplyAsync(() -> operations.execute(creator, callback), executor);
    }

    @Override
    public <T> CompletableFuture<T> execute(@Language("MySQL") @NotNull String sql, @NotNull PreparedStatementFunction<T> callback) throws DataAccessException {
        return supplyAsync(() -> operations.execute(sql, callback), executor);
    }

    @Override
    public CompletableFuture<Integer> update(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) throws DataAccessException {
        return supplyAsync(() -> operations.update(creator, setter, getGeneratedKey), executor);
    }

    @Override
    public CompletableFuture<Integer> update(@NotNull PreparedStatementCreator creator, boolean getGeneratedKeys) throws DataAccessException {
        return supplyAsync(() -> operations.update(creator, getGeneratedKeys), executor);
    }

    @Override
    public CompletableFuture<Integer> update(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) throws DataAccessException {
        return supplyAsync(() -> operations.update(sql, setter, getGeneratedKey), executor);
    }

    @Override
    public CompletableFuture<Integer> update(@Language("MySQL") @NotNull String sql, Object[] params, boolean getGeneratedKey, int... sqlTypes) throws DataAccessException {
        return supplyAsync(() -> operations.update(sql, params, getGeneratedKey, sqlTypes), executor);
    }

    @Override
    public CompletableFuture<Void> batchUpdate(@Language("MySQL") @NotNull String sql, @NotNull BatchSetter batchSetter) throws IllegalStateException {
        return runAsync(() -> operations.batchUpdate(sql, batchSetter), executor);
    }

    @Override
    public CompletableFuture<Void> batchUpdate(@Language("MySQL") @NotNull String sql, @Nullable List<Object[]> batchArgs, int... sqlTypes) throws IllegalStateException {
        return runAsync(() -> operations.batchUpdate(sql, batchArgs, sqlTypes), executor);
    }

    @Override
    public <T> CompletableFuture<Void> batchUpdate(@Language("MySQL") @NotNull String sql, @Nullable List<T> batchArgs, @NotNull ParametrizedBatchSetter<T> paramsBatchSetter) throws IllegalStateException {
        return runAsync(() -> operations.batchUpdate(sql, batchArgs, paramsBatchSetter), executor);
    }

    @Override
    public <T> CompletableFuture<T> query(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException {
        return supplyAsync(() -> operations.query(creator, setter, extractor), executor);
    }

    @Override
    public <T> CompletableFuture<T> query(@NotNull PreparedStatementCreator creator, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException {
        return supplyAsync(() -> operations.query(creator, extractor), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> query(@NotNull PreparedStatementCreator psc, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException {
        return supplyAsync(() -> operations.query(psc, resultSetRowMapper), executor);
    }

    @Override
    public <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException {
        return supplyAsync(() -> operations.query(sql, setter, extractor), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> query(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter pss, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException {
        return supplyAsync(() -> operations.query(sql, pss, resultSetRowMapper), executor);
    }

    @Override
    public <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException {
        return supplyAsync(() -> operations.query(sql, args, extractor), executor);
    }

    @Override
    public <T> CompletableFuture<T> query(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor, int... sqlTypes) throws DataAccessException {
        return supplyAsync(() -> operations.query(sql, args, extractor, sqlTypes), executor);
    }

    @Override
    public <T> CompletableFuture<T> queryOrElseGet(@NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor, Supplier<T> supplier, int... sqlTypes) throws DataAccessException {
        return supplyAsync(() -> operations.queryOrElseGet(sql, args, extractor, supplier, sqlTypes), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> queryForList(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException {
        return supplyAsync(() -> operations.queryForList(sql, args, resultSetRowMapper), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> queryForList(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper, int... sqlTypes) throws DataAccessException {
        return supplyAsync(() -> operations.queryForList(sql, args, resultSetRowMapper, sqlTypes), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> queryForListOrElseGet(@NotNull String sql, @Nullable Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<List<T>> supplier, int... sqlTypes) {
        return supplyAsync(() -> operations.queryForListOrElseGet(sql, args, resultSetRowMapper, supplier, sqlTypes), executor);
    }

    @Override
    public <T> CompletableFuture<T> queryForObject(@Language("MySQL") @NotNull String sql, Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper) throws DataAccessException {
        return supplyAsync(() -> operations.queryForObject(sql, args, resultSetRowMapper), executor);
    }

    @Override
    public <T> CompletableFuture<T> queryForObject(@Language("MySQL") @NotNull String sql, Object[] args, ResultSetRowMapper<T> resultSetRowMapper, int... sqlTypes) throws DataAccessException {
        return supplyAsync(() -> operations.queryForObject(sql, args, resultSetRowMapper, sqlTypes), executor);
    }

    @Override
    public <T> CompletableFuture<T> queryForObjectOrElseGet(@NotNull String sql, Object[] args, ResultSetRowMapper<T> resultSetRowMapper, @NotNull Supplier<T> supplier, int... sqlTypes) {
        return supplyAsync(() -> operations.queryForObjectOrElseGet(sql, args, resultSetRowMapper, supplier, sqlTypes), executor);
    }
}
