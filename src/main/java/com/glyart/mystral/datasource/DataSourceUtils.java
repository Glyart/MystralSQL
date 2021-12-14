package com.glyart.mystral.datasource;

import com.glyart.mystral.database.AsyncDatabase;
import com.glyart.mystral.database.Credentials;
import com.glyart.mystral.database.DatabaseAccessor;
import com.glyart.mystral.exceptions.ConnectionRetrieveException;
import com.glyart.mystral.exceptions.DataSourceInitException;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;

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

    @SuppressWarnings("unused")
    public static void closePool(@Nullable AsyncDatabase asyncDatabase) {
        if (asyncDatabase == null)
            return;

        asyncDatabase.getDataSource().ifPresent(DataSourceUtils::closePool);
    }

    @SuppressWarnings("unused")
    public static void closePool(@Nullable Object o) {
        if (o == null)
            return;

        if (o instanceof DataSourceSupplier) {
            DataSourceUtils.closePool(((DataSourceSupplier) o).get());
            return;
        }

        if (o instanceof DatabaseAccessor)
            DataSourceUtils.closePool((DatabaseAccessor) o);
    }

    // From https://github.com/spring-projects/spring-framework/blob/main/spring-jdbc/src/main/java/org/springframework/jdbc/support/JdbcUtils.java

    /**
     * Retrieve a JDBC column value from a ResultSet, using the specified value type.
     * <p>Uses the specifically typed ResultSet accessor methods, falling back to
     * {@link #getResultSetValue(java.sql.ResultSet, int)} for unknown types.
     * <p>Note that the returned value may not be assignable to the specified
     * required type, in case of an unknown type. Calling code needs to deal
     * with this case appropriately, e.g. throwing a corresponding exception.
     * @param rs is the ResultSet holding the data
     * @param index is the column index
     * @param requiredType the required value type (may be {@code null})
     * @return the value object (possibly not of the specified required type,
     * with further conversion steps necessary)
     * @throws SQLException if thrown by the JDBC API
     * @see #getResultSetValue(ResultSet, int)
     */
    public static Object getResultSetValue(ResultSet rs, int index, @Nullable Class<?> requiredType) throws SQLException {
        if (requiredType == null) {
            return getResultSetValue(rs, index);
        }

        Object value;

        if (String.class == requiredType) {
            return rs.getString(index);
        }
        else if (boolean.class == requiredType || Boolean.class == requiredType) {
            value = rs.getBoolean(index);
        }
        else if (byte.class == requiredType || Byte.class == requiredType) {
            value = rs.getByte(index);
        }
        else if (short.class == requiredType || Short.class == requiredType) {
            value = rs.getShort(index);
        }
        else if (int.class == requiredType || Integer.class == requiredType) {
            value = rs.getInt(index);
        }
        else if (long.class == requiredType || Long.class == requiredType) {
            value = rs.getLong(index);
        }
        else if (float.class == requiredType || Float.class == requiredType) {
            value = rs.getFloat(index);
        }
        else if (double.class == requiredType || Double.class == requiredType ||
                Number.class == requiredType) {
            value = rs.getDouble(index);
        }
        else if (BigDecimal.class == requiredType) {
            return rs.getBigDecimal(index);
        }
        else if (java.sql.Date.class == requiredType) {
            return rs.getDate(index);
        }
        else if (java.sql.Time.class == requiredType) {
            return rs.getTime(index);
        }
        else if (java.sql.Timestamp.class == requiredType || java.util.Date.class == requiredType) {
            return rs.getTimestamp(index);
        }
        else if (byte[].class == requiredType) {
            return rs.getBytes(index);
        }
        else if (Blob.class == requiredType) {
            return rs.getBlob(index);
        }
        else if (Clob.class == requiredType) {
            return rs.getClob(index);
        }
        else if (requiredType.isEnum()) {
            // Enums can either be represented through a String or an enum index value:
            // leave enum type conversion up to the caller (e.g. a ConversionService)
            // but make sure that we return nothing other than a String or an Integer.
            Object obj = rs.getObject(index);
            if (obj instanceof String) {
                return obj;
            }
            else if (obj instanceof Number) {
                return toNumber(obj);
            }
            else {
                return rs.getString(index);
            }
        }
        else {
            try {
                return rs.getObject(index, requiredType);
            }
            catch (AbstractMethodError err) {
                LOGGER.error("JDBC driver does not implement JDBC 4.1 'getObject(int, Class)' method", err);
            }
            catch (SQLFeatureNotSupportedException ex) {
                LOGGER.error("JDBC driver does not support JDBC 4.1 'getObject(int, Class)' method", ex);
            }
            catch (SQLException ex) {
                LOGGER.error("JDBC driver has limited support for JDBC 4.1 'getObject(int, Class)' method", ex);
            }

            // Corresponding SQL types for JSR-310 / Joda-Time types, left up
            // to the caller to convert them (e.g. through a ConversionService).
            String typeName = requiredType.getSimpleName();
            switch (typeName) {
                case "LocalDate":
                    return rs.getDate(index);
                case "LocalTime":
                    return rs.getTime(index);
                case "LocalDateTime":
                    return rs.getTimestamp(index);
// Fall back to getObject without type specification, again
// left up to the caller to convert the value if necessary.
                default:
                    return getResultSetValue(rs, index);
            }
        }

        // Perform was-null check if necessary (for results that the JDBC driver returns as primitives).
        return (rs.wasNull() ? null : value);
    }

    /**
     * Retrieve a JDBC column value from a ResultSet, using the most appropriate
     * value type. The returned value should be a detached value object, not having
     * any ties to the active ResultSet: in particular, it should not be a Blob or
     * Clob object but rather a byte array or String representation, respectively.
     * <p>Uses the {@code getObject(index)} method, but includes additional "hacks"
     * to get around Oracle 10g returning a non-standard object for its TIMESTAMP
     * datatype and a {@code java.sql.Date} for DATE columns leaving out the
     * time portion: These columns will explicitly be extracted as standard
     * {@code java.sql.Timestamp} object.
     * @param rs is the ResultSet holding the data
     * @param index is the column index
     * @return the value object
     * @throws SQLException if thrown by the JDBC API
     * @see java.sql.Blob
     * @see java.sql.Clob
     * @see java.sql.Timestamp
     */
    @Nullable
    public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
        Object obj = rs.getObject(index);
        String className = null;
        if (obj != null) {
            className = obj.getClass().getName();
        }
        if (obj instanceof Blob) {
            Blob blob = (Blob) obj;
            obj = blob.getBytes(1, (int) blob.length());
        }
        else if (obj instanceof Clob) {
            Clob clob = (Clob) obj;
            obj = clob.getSubString(1, (int) clob.length());
        }
        else if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
            obj = rs.getTimestamp(index);
        }
        else if (className != null && className.startsWith("oracle.sql.DATE")) {
            String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                obj = rs.getTimestamp(index);
            }
            else {
                obj = rs.getDate(index);
            }
        }
        else if (obj instanceof java.sql.Date) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                obj = rs.getTimestamp(index);
            }
        }
        return obj;
    }

    private static int toNumber(Object o) {
        if (!(o instanceof Number)) {
            return Integer.MIN_VALUE;
        }

        Number number = (Number) o;
        BigInteger bigInt = null;
        if (number instanceof BigInteger) {
            bigInt = (BigInteger) number;
        }
        else if (number instanceof BigDecimal) {
            bigInt = ((BigDecimal) number).toBigInteger();
        }

        if (bigInt != null && (bigInt.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 || bigInt.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)) {
            throw new RuntimeException("Number out of range.");
        }
        long longValue = number.longValue();
        if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
            throw new RuntimeException("Number out of range.");
        }

        return number.intValue();
    }

    public static String getColumnName(@NotNull ResultSetMetaData metaData, int i) throws SQLException {
        String columnName = Preconditions.checkNotNull(metaData).getColumnLabel(i); // "As" keyword sql
        if (columnName == null || columnName.isEmpty()) {
            columnName = metaData.getColumnName(i); // real column name
        }
        return columnName;
    }
}