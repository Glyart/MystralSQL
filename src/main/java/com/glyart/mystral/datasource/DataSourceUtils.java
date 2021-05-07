package com.glyart.mystral.datasource;

import com.glyart.mystral.exceptions.DataSourceInitException;
import com.glyart.mystral.sql.AsyncDataOperations;
import com.glyart.mystral.database.Credentials;
import com.glyart.mystral.database.DatabaseAccessor;
import com.glyart.mystral.exceptions.ConnectionRetrieveException;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class that deals with {@link DataSource}s and its related objects.
 */
public final class DataSourceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceUtils.class);

    private DataSourceUtils() {}

    /**
     * Creates a new DataSource with the given Credentials. This is done by using the default implementation of
     * {@link DataSourceFactory}: {@link HikariFactory}
     * @param credentials the credentials
     * @return a new DataSource
     * @throws DataSourceInitException if something went wrong during the creation of the DataSource
     * @see DataSourceFactory
     * @see HikariFactory
     */
    @NotNull
    public static DataSource newDataSource(@NotNull Credentials credentials) throws DataSourceInitException {
        Preconditions.checkNotNull(credentials, "The credentials cannot be null.");
        HikariFactory factory = new HikariFactory();
        factory.setCredentials(credentials);
        try {
            return factory.newDataSource();
        } catch (Exception e) {
            throw new DataSourceInitException("Cannot create the data source. ", e);
        }
    }

    /**
     * Simply gets a connection from a given valid DataSource.
     * @param dataSource the datasource
     * @return a new connection
     */
    @Nullable
    public static Connection getConnection(@NotNull DataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "The DataSource cannot be null.");
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionRetrieveException("Cannot retrieve connection from the given DataSource.", e);
        }
    }

    /**
     * Simply closes a connection.
     * @param connection the connection
     */
    public static void closeConnection(@Nullable Connection connection) {
        if (connection == null)
            return;

        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.warn("There was an error while trying to close the connection.", e);
        }
    }

    public static void closeResultSet(@Nullable ResultSet resultSet) {
        if (resultSet == null)
            return;

        try {
            resultSet.close();
        } catch (Throwable e) {
            LOGGER.warn("There was an error while trying to close the connection.", e);
        }
    }

    public static void closeStatement(@Nullable Statement statement) {
        if (statement == null)
            return;

        try {
            statement.close();
        } catch (Throwable e) {
            LOGGER.warn("There was an error while trying to close the statement", e);
        }
    }

    /**
     * Closes a connection pool managed by a DataSource.
     * <p><b>NOTE: the provided DataSource must implement the interface {@link Closeable}</b></p>
     * @param dataSource the DataSource
     * @see Closeable
     */
    public static void closePool(@Nullable DataSource dataSource) {
        if (dataSource == null)
            return;

        Preconditions.checkState(dataSource instanceof Closeable, "The provided DataSource is not instance of Closeable.");

        Closeable connectionPool = (Closeable) dataSource;
        try {
            connectionPool.close();
        } catch (IOException e) {
            LOGGER.warn("There was an error while closing the connection pool.", e);
        }
    }

    public static void closePool(@Nullable DatabaseAccessor accessor) {
        if (accessor == null)
            return;

        DataSourceUtils.closePool(accessor.getDataSource());
    }

    public static void closePool(@Nullable AsyncDataOperations asyncDataOperations) {
        if (asyncDataOperations == null)
            return;

        DataSourceUtils.closePool(((DatabaseAccessor) asyncDataOperations).getDataSource());
    }
}