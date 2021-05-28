package com.glyart.mystral.datasource;

import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * Represents a supplier of DataSource.
 *
 * <p>There is no requirement that a new or distinct DataSource be returned each
 * time the supplier is invoked.
 *
 * @see DataSource
 */
@FunctionalInterface
public interface DataSourceSupplier extends Supplier<DataSource> {

    /**
     * Gets a DataSource.
     * @return a DataSource.
     */
    @Override
    @NotNull
    DataSource get();
}
