package com.glyart.mystral.database;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

/**
 * Utility class for {@link PreparedStatement} objects.
 */
public final class PreparedStatementUtils {

    private static final int CLOB_LENGTH = 4000;

    private PreparedStatementUtils() {}

    /**
     * Sets a value inside a {@link PreparedStatement} parameter.
     * @param statement a valid PreparedStatement
     * @param paramIndex the parameter index
     * @param sqlType the type of the parameter expressed as sql {@link Types}
     * @param value the parameter value
     * @throws SQLException if something went wrong
     */
    public static void setValue(@NotNull PreparedStatement statement, int paramIndex, int sqlType, @Nullable Object value) throws SQLException {
        Preconditions.checkNotNull(statement, "The PreparedStatement cannot be null.");
        if (value == null) {
            statement.setNull(paramIndex, sqlType);
            return;
        }

        switch (sqlType) {
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                statement.setString(paramIndex, value.toString());
                break;
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                statement.setNString(paramIndex, value.toString());
                break;
            case Types.CLOB:
            case Types.NCLOB:
                if (isStringValue(value.getClass())) {
                    String stringValue = value.toString();
                    if (stringValue.length() > CLOB_LENGTH) {
                        if (sqlType == Types.NCLOB)
                            statement.setNClob(paramIndex, new StringReader(stringValue), stringValue.length());
                        else
                            statement.setClob(paramIndex, new StringReader(stringValue), stringValue.length());
                    } else {
                        if (sqlType == Types.NCLOB)
                            statement.setNString(paramIndex, stringValue);
                        else
                            statement.setString(paramIndex, stringValue);
                    }
                }
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                if (value instanceof BigDecimal)
                    statement.setBigDecimal(paramIndex, (BigDecimal) value);
                else
                    statement.setObject(paramIndex, value, sqlType);

                break;
            case Types.BOOLEAN:
                if (value instanceof Boolean)
                    statement.setBoolean(paramIndex, (Boolean) value);
                else
                    statement.setObject(paramIndex, value, Types.BOOLEAN);

                break;
            case Types.DATE:
                if (value instanceof java.util.Date) {
                    if (value instanceof java.sql.Date)
                        statement.setDate(paramIndex, (java.sql.Date) value);
                    else
                        statement.setDate(paramIndex, new java.sql.Date(((java.util.Date) value).getTime()));
                }
                else if (value instanceof Calendar) {
                    Calendar cal = (Calendar) value;
                    statement.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
                }
                else
                    statement.setObject(paramIndex, value, Types.DATE);

                break;
            case Types.TIME:
                if (value instanceof java.util.Date) {
                    if (value instanceof java.sql.Time)
                        statement.setTime(paramIndex, (java.sql.Time) value);
                    else
                        statement.setTime(paramIndex, new java.sql.Time(((java.util.Date) value).getTime()));
                }
                else if (value instanceof Calendar) {
                    Calendar cal = (Calendar) value;
                    statement.setTime(paramIndex, new java.sql.Time(cal.getTime().getTime()), cal);
                }
                else
                    statement.setObject(paramIndex, value, Types.TIME);

                break;
            case Types.TIMESTAMP:
                if (value instanceof java.util.Date) {
                    if (value instanceof java.sql.Timestamp) {
                        statement.setTimestamp(paramIndex, (java.sql.Timestamp) value);
                    }
                    else {
                        statement.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) value).getTime()));
                    }
                }
                else if (value instanceof Calendar) {
                    Calendar cal = (Calendar) value;
                    statement.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
                }
                else {
                    statement.setObject(paramIndex, value, Types.TIMESTAMP);
                }
                break;
            default:
                statement.setObject(paramIndex, value, sqlType);
        }
    }

    private static boolean isStringValue(Class<?> valueType) {
        return (CharSequence.class.isAssignableFrom(valueType) || StringWriter.class.isAssignableFrom(valueType));
    }
}

// I'm on a SugarCrash!