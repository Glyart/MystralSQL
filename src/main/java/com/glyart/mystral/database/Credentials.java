package com.glyart.mystral.database;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;

/**
 * Represents the credentials for a connection to a data source that <b>supports connection pooling</b>.
 * Multiple different connections can be established by creating other Credentials instances.
 */
public class Credentials {

    @NotNull
    private final String hostname;

    private int port;

    @NotNull
    private final String username;

    @Nullable
    private String password;

    @Nullable
    private String schema;

    @NotNull
    private final String poolName;

    protected Credentials(@NotNull String hostname, @NotNull String username, @NotNull String poolName) {
        this.hostname = hostname;
        this.username = username;
        this.poolName = poolName;
    }
    /**
     * Creates a new builder for Credentials.
     * @return the builder
     */
    @NotNull
    public static CredentialsBuilder builder() {
        return new CredentialsBuilder();
    }

    /**
     * Gets the hostname for connecting to a data source.
     * @return the datasource
     */
    @NotNull
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the port for connecting to a data source.
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the username for connecting to a data source.
     * @return the username
     */
    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password for connecting to a data source.
     * @return the password
     */
    @Nullable
    public String getPassword() {
        return password;
    }

    /**
     * Gets the schema for connecting to a data source.
     * @return the schema
     */
    @Nullable
    public String getSchema() {
        return schema;
    }

    /**
     * Gets the name of the connection pool.
     * @return the name of the pool
     */
    @NotNull
    public String getPoolName() {
        return poolName;
    }

    /**
     * Represents a builder for creating {@link Credentials} objects.
     */
    public static final class CredentialsBuilder {

        private String hostname;
        private int port;
        private String username;
        private String password;
        private String schema;
        private String poolName;

        private CredentialsBuilder() {
            this.port = 3306;
        }

        /**
         * Sets the hostname
         * @param hostName the hostname
         * @return this builder instance
         */
        public CredentialsBuilder host(@NotNull String hostName) {
            Preconditions.checkArgument(!Preconditions.checkNotNull(hostName, "Hostname cannot be null.").isEmpty(),
                    "Hostname cannot be empty.");
            this.hostname = hostName;
            return this;
        }

        /**
         * Sets the port for connecting to a data source.
         * @param port the port of the physical data source
         * @return this builder instance
         */
        public CredentialsBuilder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the username for connecting to a data source.
         * @param username the username
         * @return this builder instance
         */
        public CredentialsBuilder user(@NotNull String username) {
            Preconditions.checkArgument(!Preconditions.checkNotNull(username, "Username cannot be null.").isEmpty(),
                    "Username cannot be empty.");
            this.username = username;
            return this;
        }

        /**
         * Sets the password for connecting to a data source.
         * @param password the password
         * @return this builder instance
         */
        public CredentialsBuilder password(@Nullable String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the initial schema to use after the connection to a data source is successfully established.<br>
         * <b>This will not provide the creation of the schema if it doesn't exist.</b>
         * @param schema an existing database schema
         * @return this builder instance
         */
        public CredentialsBuilder schema(@Nullable String schema) {
            this.schema = schema;
            return this;
        }

        /**
         * Sets the name of the data source connection pool.
         * @param poolName the name of the pool
         * @return this builder instance
         */
        public CredentialsBuilder pool(@NotNull String poolName) {
            Preconditions.checkArgument(!Preconditions.checkNotNull(poolName, "Pool name cannot be null.").isEmpty(),
                    "Pool name cannot be empty.");
            this.poolName = poolName;
            return this;
        }

        /**
         * Builds a {@link Credentials} object, ready to be passed to {@link Mystral#newDatabase(Credentials)}.<br>
         * If you want async usage you may need {@link Mystral#newAsyncDatabase(Credentials, Executor)}.
         * @return a new instance of Credentials.
         * @see Mystral
         */
        @NotNull
        public Credentials build() {
            Credentials credentials = new Credentials(hostname, username, poolName);

            credentials.port = port;
            credentials.password = password;
            credentials.schema = schema;

            return credentials;
        }
    }
}
