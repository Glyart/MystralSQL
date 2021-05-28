package com.glyart.mystral.database;

import com.glyart.mystral.datasource.DataSourceSupplier;
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
        this.dataSource = checkDataSource(dataSource);
    }

    protected DatabaseAccessor(@NotNull DataSourceSupplier supplier) {
        Preconditions.checkNotNull(supplier, "The DataSourceSupplier cannot be null.");
        this.dataSource = checkDataSource(supplier.get());
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

    private DataSource checkDataSource(DataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "The DataSource cannot be null.");
        return dataSource;
    }
}
