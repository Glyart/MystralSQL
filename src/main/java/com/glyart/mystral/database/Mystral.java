package com.glyart.mystral.database;

import com.glyart.mystral.datasource.DataSourceFactory;
import com.glyart.mystral.datasource.DataSourceUtils;
import com.glyart.mystral.exceptions.DataSourceInitException;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

/**
 * Utility class for fast instantiating {@link Database} and {@link AsyncDatabase} objects.
 */
public final class Mystral {

    private Mystral() {}

    /**
     * Creates a new Database with the given Credentials.
     * @param credentials the Credentials for creating a connection
     * @return the Database
     * @throws DataSourceInitException if something went wrong
     * @see Database
     * @see Credentials
     * @see DataSourceFactory#newDataSource()
     */
    @NotNull
    public static Database newDatabase(@NotNull Credentials credentials) throws DataSourceInitException {
        return new Database(DataSourceUtils.newDataSource(credentials));
    }

    /**
     * Creates a new Database with the given DataSourceFactory.
     * @param factory the factory
     * @param <T> the DataSource Type
     * @return the Database
     * @throws DataSourceInitException if something went wrong
     * @see DataSourceFactory
     * @see DataSourceFactory#newDataSource()
     */
    @NotNull
    public static <T extends DataSource> Database newDatabase(@NotNull DataSourceFactory<T> factory) throws DataSourceInitException {
        Preconditions.checkNotNull(factory, "The DataSourceFactory cannot be null.");
        try {
            return new Database(factory.newDataSource());
        } catch (Exception e) {
            throw new DataSourceInitException("Cannot create the data source.", e);
        }
    }

    /**
     * Creates a new AsyncDatabase with the given Credentials and an Executor implementation.
     * @param credentials the credentials for creating a connection
     * @param executor the tasks executor
     * @return the AsyncDatabase
     * @throws DataSourceInitException if something went wrong
     * @see AsyncDatabase
     * @see Executor
     * @see DataSourceFactory#newDataSource()
     */
    @NotNull
    public static AsyncDatabase newAsyncDatabase(@NotNull Credentials credentials, @NotNull Executor executor) throws DataSourceInitException {
        Preconditions.checkNotNull(executor, "The executor cannot be null.");
        return new AsyncDatabase(Mystral.newDatabase(credentials), executor);
    }

    /**
     * Creates a new AsyncDatabase with the given DataSourceFactory and an Executor implementation.
     * @param factory the factory
     * @param executor the tasks executor
     * @param <T> the DataSource type
     * @return the AsyncDatabase
     * @throws DataSourceInitException if something went wrong
     * @see DataSourceFactory#newDataSource()
     */
    @NotNull
    public static <T extends DataSource> AsyncDatabase newAsyncDatabase(@NotNull DataSourceFactory<T> factory, @NotNull Executor executor) throws DataSourceInitException {
        Preconditions.checkNotNull(executor, "The executor cannot be null.");
        return new AsyncDatabase(Mystral.newDatabase(factory), executor);
    }
}
