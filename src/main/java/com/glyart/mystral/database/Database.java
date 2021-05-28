package com.glyart.mystral.database;

import com.glyart.mystral.datasource.DataSourceSupplier;
import com.glyart.mystral.datasource.DataSourceUtils;
import com.glyart.mystral.exceptions.DataAccessException;
import com.glyart.mystral.sql.*;
import com.glyart.mystral.sql.impl.*;
import com.google.common.base.Preconditions;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * Represents implementation of basic synchronous operations for interacting with a data source.
 * <p>An instance of this class is available by passing a valid {@link DataSource}.
 *
 * <p>This class:
 * <ul>
 *     <li>works on the same thread that invokes its methods</li>
 *     <li>executes all the <a href="https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">CRUD</a> operations on a data source</li>
 *     <li>handles exceptions and communicates the sql that generated the problem (if possible)</li>
 *     <li>gives <b>nullable results</b></li>
 *     <li>iterates over {@link java.sql.ResultSet}s</li>
 *     <li>deals with static and prepared statements</li>
 * </ul>
 *
 * <p>Methods of this class use various callback interfaces. A reading of those is greatly suggested.
 *
 * <br> Since the callback interfaces make {@link Database}'s methods parameterizable, there should be no need to subclass this class.</p>
 * @see BatchSetter
 * @see ParametrizedBatchSetter
 * @see PreparedStatementFunction
 * @see PreparedStatementCreator
 * @see ResultSetExtractor
 * @see ResultSetRowMapper
 * @see StatementFunction
 */
public class Database extends DatabaseAccessor implements DataOperations, DataSourceSupplier {

    /**
     * Constructs a new Database object with the given {@link DataSource}.
     * @param dataSource the DataSource
     */
    public Database(@NotNull DataSource dataSource) {
        super(dataSource);
        super.setLogger(LoggerFactory.getLogger(this.getClass()));
    }

    @Override
    public <T> T execute(@NotNull StatementFunction<T> callback) throws DataAccessException {
        Preconditions.checkNotNull(callback, "StatementCallback cannot be null.");
        Connection connection = DataSourceUtils.getConnection(dataSource);
        if (connection == null) {
            logger.warn("Cannot retrieve a connection.");
            return null;
        }
        T result;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            result = callback.apply(statement);
        } catch (SQLException e) {
            throw new DataAccessException("StatementFunction Callback", getSql(callback), e);
        } finally {
            DataSourceUtils.closeStatement(statement);
            DataSourceUtils.closeConnection(connection);
        }
        return result;
    }

    @Override
    public int update(@Language("MySQL") @NotNull String sql, boolean getGeneratedKeys) {
        Preconditions.checkNotNull(sql, "Sql statement cannot be null.");
        if (getGeneratedKeys)
            return update(sql, null, true);

        SimpleUpdateStatementFunction simpleUpdateStatementFunction = new SimpleUpdateStatementFunction(sql);
        Object result = execute(simpleUpdateStatementFunction);
        return result == null ? -1 : (int) result;
    }

    @Override
    public <T> T query(@Language("MySQL") @NotNull String sql, ResultSetExtractor<T> extractor) {
        return execute(new QueryStatementFunction<>(extractor, sql));
    }

    @Override
    public <T> List<T> queryForList(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper) {
        return query(sql, new DefaultExtractor<>(resultSetRowMapper));
    }

    @Override
    public <T> T queryForObject(@Language("MySQL") @NotNull String sql, ResultSetRowMapper<T> resultSetRowMapper) {
        List<T> resultList = query(sql, new DefaultExtractor<>(resultSetRowMapper, 1));
        T result = resultList == null || resultList.isEmpty() ? null : resultList.get(0);
        if (result == null)
            logger.warn("queryForObject(String, ResultSetRowMapper) was invoked but a single object was not returned.");

        return result;
    }

    // Prepared statements

    @Override
    public <T> T execute(@NotNull PreparedStatementCreator creator, @NotNull PreparedStatementFunction<T> callback) {
        Preconditions.checkNotNull(creator, "PreparedStatementCreator cannot be null.");
        Preconditions.checkNotNull(callback, "PreparedStatementFunction cannot be null.");
        Connection connection = DataSourceUtils.getConnection(dataSource);
        if (connection == null) {
            logger.warn("Cannot retrieve a connection.");
            return null;
        }

        PreparedStatement ps = null;
        T result;
        try {
            ps = creator.create(connection);
            result = callback.apply(ps);
        } catch (SQLException e) {
            throw new DataAccessException("PreparedStatementFunction Callback", getSql(creator), e);
        } finally {
            DataSourceUtils.closeStatement(ps);
            DataSourceUtils.closeConnection(connection);
        }

        return result;
    }

    @Override
    public <T> T execute(@Language("MySQL") @NotNull String sql, @NotNull PreparedStatementFunction<T> callback) {
        return execute(new DefaultCreator(sql), callback);
    }

    @Override
    public int update(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) {
        PreparedStatementFunction<Integer> function = ps -> {
            ResultSet set = null;
            int rows;
            try {
                if (setter != null)
                    setter.setValues(ps);

                if (getGeneratedKey) {
                    ps.executeUpdate();
                    set = ps.getGeneratedKeys();
                    rows = set.next() ? set.getInt(1) : 0;
                } else
                    rows = ps.executeUpdate();
            } finally {
                DataSourceUtils.closeResultSet(set);
            }
            return rows;
        };
        Object result = execute(creator, function);
        return result == null ? -1 : (int) result;
    }

    @Override
    public int update(@NotNull PreparedStatementCreator creator, boolean getGeneratedKeys) {
        return update(creator, null, getGeneratedKeys);
    }

    @Override
    public int update(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) {
        return update(new DefaultCreator(sql, getGeneratedKey), setter, getGeneratedKey);
    }

    @Override
    public int update(@Language("MySQL") @NotNull String sql, Object[] params, boolean getGeneratedKey, int... sqlTypes) {
        return update(sql, new DefaultSetter(params, sqlTypes), getGeneratedKey);
    }

    @Override
    public void batchUpdate(@Language("MySQL") @NotNull String sql, @NotNull BatchSetter batchSetter) throws IllegalStateException {
        Preconditions.checkNotNull(sql, "Sql cannot be null.");
        Preconditions.checkNotNull(batchSetter, "BatchPreparedStatementSetter cannot be null.");

        execute(sql, ps -> {
            if (!ps.getConnection().getMetaData().supportsBatchUpdates())
                throw new IllegalStateException("This driver doesn't support batch updates. This method will remain unusable until you choose a driver that supports batch updates.");

            for (int i = 0; i < batchSetter.getBatchSize(); i++) {
                batchSetter.setValues(ps, i);
                ps.addBatch();
            }
            ps.executeBatch();
            return null;
        });
    }

    @Override
    public void batchUpdate(@Language("MySQL") @NotNull String sql, @Nullable List<Object[]> batchArgs, int... sqlTypes) throws IllegalStateException {
        Preconditions.checkNotNull(sql, "Sql cannot be null.");
        if (batchArgs == null || batchArgs.isEmpty())
            return;

        batchUpdate(sql, new DefaultBatchSetter(batchArgs, sqlTypes));
    }

    @Override
    public <T> void batchUpdate(@Language("MySQL") @NotNull String sql, @Nullable List<T> batchArgs, @NotNull ParametrizedBatchSetter<T> paramsBatchSetter) throws IllegalStateException {
        Preconditions.checkNotNull(sql, "Sql cannot be null.");
        Preconditions.checkArgument(!sql.isEmpty(), "Sql cannot be empty.");
        Preconditions.checkNotNull(paramsBatchSetter, "ParametrizedPreparedStatementSetter cannot be null.");

        if (batchArgs == null || batchArgs.isEmpty())
            return;

        execute(sql, ps -> {
            if (!ps.getConnection().getMetaData().supportsBatchUpdates())
                throw new IllegalStateException("This driver doesn't support batch updates. This method will remain unusable until you choose a driver that supports batch updates.");

            for (T batchParam : batchArgs) {
                paramsBatchSetter.setValues(ps, batchParam);
                ps.addBatch();
            }
            ps.executeBatch();
            return null;
        });
    }

    @Override
    public <T> T query(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<T> extractor) {
        Preconditions.checkNotNull(extractor, "ResultSetExtractor cannot be null.");

        return execute(creator, ps -> {
            T result;
            ResultSet resultSet = null;
            try {
                if (setter != null)
                    setter.setValues(ps);

                resultSet = ps.executeQuery();
                result = extractor.extractData(resultSet);
            } finally {
                DataSourceUtils.closeResultSet(resultSet);
            }
            return result;
        });
    }

    @Override
    public <T> T query(@NotNull PreparedStatementCreator creator, @NotNull ResultSetExtractor<T> extractor) {
        return query(creator, null, extractor);
    }

    @Override
    public <T> List<T> query(@NotNull PreparedStatementCreator psc, @NotNull ResultSetRowMapper<T> resultSetRowMapper) {
        return query(psc, new DefaultExtractor<>(resultSetRowMapper));
    }

    @Override
    public <T> T query(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<T> extractor) {
        return query(new DefaultCreator(sql), setter, extractor);
    }

    @Override
    public <T> List<T> query(@Language("MySQL") @NotNull String sql, @Nullable PreparedStatementSetter pss, @NotNull ResultSetRowMapper<T> resultSetRowMapper) {
        return query(sql, pss, new DefaultExtractor<>(resultSetRowMapper));
    }

    @Override
    public <T> T query(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor) throws DataAccessException {
        return query(sql, new DefaultSetterUnknownType(args), extractor);
    }

    @Override
    public <T> T query(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<T> extractor, int... sqlTypes) {
        return query(sql, new DefaultSetter(args, sqlTypes), extractor);
    }

    @Override
    public <T> List<T> queryForList(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper) {
        return query(sql, args, new DefaultExtractor<>(resultSetRowMapper));
    }

    @Override
    public <T> List<T> queryForList(@Language("MySQL") @NotNull String sql, @Nullable Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper, int... sqlTypes) {
        return query(sql, args, new DefaultExtractor<>(resultSetRowMapper), sqlTypes);
    }

    @Override
    public <T> T queryForObject(@Language("MySQL") @NotNull String sql, Object[] args, @NotNull ResultSetRowMapper<T> resultSetRowMapper) {
        List<T> resultList = query(sql, args, new DefaultExtractor<>(resultSetRowMapper, 1));
        return nullableResult(resultList, "queryForObject(String, Object[], ResultSetRowMapper) was invoked but a single object was not returned.");
    }

    @Override
    public <T> T queryForObject(@Language("MySQL") @NotNull String sql, Object[] args, ResultSetRowMapper<T> resultSetRowMapper, int... sqlTypes) {
        List<T> resultList = query(sql, args, new DefaultExtractor<>(resultSetRowMapper, 1), sqlTypes);
        return nullableResult(resultList, "queryForObject(String, Object[], int[], ResultSetRowMapper) was invoked but a single object was not returned.");
    }

    @Nullable
    private <T> T nullableResult(List<T> resultList, String msg) {
        T result = resultList.isEmpty() ? null : resultList.get(0);
        if (resultList.size() != 1)
            logger.warn(msg);

        return result;
    }

    @Nullable
    protected String getSql(Object o) {
        if (!(o instanceof SqlProvider))
            return null;

        return ((SqlProvider) o).getSql();
    }

    @Override
    public @NotNull DataSource get() {
        return getDataSource();
    }
}
