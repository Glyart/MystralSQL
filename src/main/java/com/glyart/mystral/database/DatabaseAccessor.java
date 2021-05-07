package com.glyart.mystral.database;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;

/**
 * This class is used as base class for {@link Database} and for common properties.
 */
public abstract class DatabaseAccessor {

    protected final DataSource dataSource;
    protected Logger logger;

    protected DatabaseAccessor(@NotNull DataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "The DataSource cannot be null.");
        this.dataSource = dataSource;
    }

    /**
     * Returns the {@link DataSource} used by {@link DatabaseAccessor}'s subclass.
     * @return the DataSource
     */
    @NotNull
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the logger used by {@link DatabaseAccessor}'s subclasses.
     * @param logger the logger
     */
    public void setLogger(@NotNull Logger logger) {
        Preconditions.checkNotNull(logger, "The logger cannot be null.");
        this.logger = logger;
    }
}
