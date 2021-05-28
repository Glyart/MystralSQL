package com.glyart.mystral.sql.impl;

import com.glyart.mystral.sql.PreparedStatementCreator;
import com.glyart.mystral.sql.SqlProvider;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DefaultCreator implements PreparedStatementCreator, SqlProvider {

    @NotNull
    private final String sql;
    private final boolean getGeneratedKeys;

    public DefaultCreator(@NotNull String sql) {
        this(sql, false);
    }

    public DefaultCreator(@NotNull String sql, boolean getGeneratedKeys) {
        Preconditions.checkNotNull(sql, "Sql statement cannot be null.");
        Preconditions.checkArgument(!sql.isEmpty(), "Sql statement cannot be empty.");
        this.sql = sql;
        this.getGeneratedKeys = getGeneratedKeys;
    }

    @NotNull
    @Override
    public PreparedStatement create(@NotNull Connection connection) throws SQLException {
        Preconditions.checkNotNull(connection, "Connection cannot be null.");
        return connection.prepareStatement(sql, getGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }

    @NotNull
    @Override
    public String getSql() {
        return sql;
    }
}