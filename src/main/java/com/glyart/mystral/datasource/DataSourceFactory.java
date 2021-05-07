package com.glyart.mystral.datasource;

import com.glyart.mystral.exceptions.DataSourceInitException;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;

/**
 * Convenient interface for lazily creating {@link DataSource} objects.
 * This interface exposes the {@link #newDataSource()} method, which inits the DataSource.
 *
 * <p><b>Knowing this, implementations must call {@link #newDataSource()} when it's all set.</b></p>
 * <p>This interface can be used to create any datasource which supports a property-based configuration.</p>
 * @param <T> the DataSource type
 * @see DataSource
 * @see HikariFactory
 */
@FunctionalInterface
public interface DataSourceFactory<T extends DataSource> {

    /**
     * Represents a basic template url for creating a jdbc url.<br>
     * The structure is <b>{@code jdbc:<dbms-name>://<host>:<port>/<schema>}</b><br>
     * More info <a href=https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html>here</a>.
     */
    String TEMPLATE_URL = "jdbc:%s://%s:%s/%s";

    /**
     * If ready, a new DataSource is created with its configuration.
     * @return the DataSource
     * @throws DataSourceInitException if something went wrong
     */
    @NotNull
    T newDataSource() throws DataSourceInitException;
}
