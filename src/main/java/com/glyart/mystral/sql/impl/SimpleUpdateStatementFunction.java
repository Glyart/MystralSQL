package com.glyart.mystral.sql.impl;

import com.glyart.mystral.sql.SqlProvider;
import com.glyart.mystral.sql.StatementFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Statement;

public class SimpleUpdateStatementFunction implements StatementFunction<Integer>, SqlProvider {

    @Nullable
    private final String sql;

    public SimpleUpdateStatementFunction(@Nullable String sql) {
        this.sql = sql;
    }

    @Override
    public Integer apply(@NotNull Statement statement) throws SQLException {
        return statement.executeUpdate(sql);
    }

    @Nullable
    @Override
    public String getSql() {
        return sql;
    }
}
