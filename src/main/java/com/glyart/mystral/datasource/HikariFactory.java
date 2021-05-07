package com.glyart.mystral.datasource;

import com.glyart.mystral.database.Credentials;
import com.glyart.mystral.exceptions.DataSourceInitException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a component that lazily initializes a {@link HikariDataSource}.<br>
 * <b>This prevents errors during instantiation via constructor.</b><br>
 */
public class HikariFactory implements DataSourceFactory<HikariDataSource> {

    protected static final Map<String, String> PROPS = ImmutableMap.<String, String>builder()
            .put("useUnicode", "true")
            .put("characterEncoding", "utf8")

            // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
            .put("cachePrepStmts", "true")
            .put("prepStmtCacheSize", "250")
            .put("prepStmtCacheSqlLimit", "2048")
            .put("useServerPrepStmts", "true")
            .put("useLocalSessionState", "true")
            .put("rewriteBatchedStatements", "true")
            .put("cacheResultSetMetadata", "true")
            .put("cacheServerConfiguration", "true")
            .put("elideSetAutoCommits", "true")
            .put("maintainTimeStats", "false")
            .put("alwaysSendSetIsolation", "false")
            .put("cacheCallableStmts", "true")

            // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery
            .put("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)))
            .build();

    // https://github.com/lucko/helper/blob/master/helper-sql/src/main/java/me/lucko/helper/sql/plugin/HelperSql.java
    protected static final int MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1;
    protected static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);

    protected static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30);
    protected static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    protected static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(10);

    protected final HikariConfig hikariConfig;

    protected HikariDataSource hikari;
    protected Credentials credentials;

    @NotNull
    public HikariConfig getHikariConfig() {
        return hikariConfig;
    }

    @Nullable
    public HikariDataSource getHikari() {
        return hikari;
    }

    @Nullable
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Gets a never null HikariDataSource.
     * @return the HikariDataSource
     * @throws IllegalStateException if the DataSource is not set
     */
    protected HikariDataSource obtainHikari() {
        HikariDataSource hikariDataSource = getHikari();
        Preconditions.checkState(hikariDataSource != null, "HikariDataSource is not set.");
        return hikariDataSource;
    }

    /**
     * Constructs a new {@link HikariFactory} with a empty {@link HikariConfig}.
     */
    public HikariFactory() {
        this(new HikariConfig());
    }

    /**
     * Constructs a new {@link HikariFactory} with the given {@link HikariConfig}.
     * @param hikariConfig the config
     */
    public HikariFactory(@NotNull HikariConfig hikariConfig) {
        Preconditions.checkNotNull(hikariConfig, "HikariConfig cannot be null.");
        this.hikariConfig = hikariConfig;
    }

    /**
     * Sets the Credentials used for connecting to a data source.
     * @param credentials the credentials
     * @param <T> this instance type
     * @return this instance
     */
    @SuppressWarnings("unchecked")
    public <T extends HikariFactory> T setCredentials(@Nullable Credentials credentials) {
        this.credentials = credentials;
        return (T) this;
    }

    /**
     * Adds a set of properties in the DataSource configuration.
     * @param properties the properties
     * @param <T> this instance type
     * @return this instance
     */
    @SuppressWarnings("unchecked")
    public <T extends HikariFactory> T addProperties(@Nullable Properties properties) {
        if (properties == null)
            return (T) this;

        hikariConfig.setDataSourceProperties(properties);
        return (T) this;
    }

    /**
     * Adds the property in the DataSource configuration. If one of the arguments is null then the property is not set.
     * @param key the property's key
     * @param val the value of the property
     * @param <T> this instance typeù
     * @return this instance
     * @throws IllegalArgumentException if the key is empty
     */
    @SuppressWarnings("unchecked")
    public <T extends HikariFactory> T setProperty(@Nullable String key, @Nullable Object val) {
        if (key == null || val == null)
            return (T) this;

        Preconditions.checkArgument(key.isEmpty(), "The key cannot be empty.");

        hikariConfig.addDataSourceProperty(key, val);
        return (T) this;
    }

    @Override
    @NotNull
    public HikariDataSource newDataSource() throws DataSourceInitException {
        if (credentials != null) {
            hikariConfig.setJdbcUrl(String.format(TEMPLATE_URL, "mysql", credentials.getHostname(), credentials.getPort(), credentials.getSchema()));
            hikariConfig.setUsername(credentials.getUsername());
            hikariConfig.setPassword(credentials.getPassword());
        }
        PROPS.forEach(hikariConfig::addDataSourceProperty);
        hikariConfig.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        hikariConfig.setMinimumIdle(MINIMUM_IDLE);
        hikariConfig.setMaxLifetime(MAX_LIFETIME);
        hikariConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        hikariConfig.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);
        try {
            this.hikari = new HikariDataSource(hikariConfig);
        } catch (RuntimeException e) {
            throw new DataSourceInitException("Cannot create the DataSource " + hikariConfig.getDataSourceClassName(), e);
        }
        return obtainHikari();
    }
}
