package com.glyart.mystral.sql.impl;

import com.glyart.mystral.sql.SqlProvider;
import com.glyart.mystral.sql.StatementFunction;
import com.glyart.mystral.datasource.DataSourceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateStatementFunction implements StatementFunction<Integer>, SqlProvider {

    @Nullable
    private final String sql;
    private final boolean getGeneratedKeys;

    public UpdateStatementFunction(@Nullable String sql, boolean getGeneratedKeys) {
        this.sql = sql;
        this.getGeneratedKeys = getGeneratedKeys;
    }

    @Override
    public Integer apply(@NotNull Statement statement) throws SQLException {
        int rows;
        ResultSet set = null;
        try {
            if (getGeneratedKeys) {
                statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                set = statement.getGeneratedKeys();
                rows = set.next() ? set.getInt(1) : 0;
            } else
                rows = statement.executeUpdate(sql, Statement.NO_GENERATED_KEYS);
        } finally {
            DataSourceUtils.closeResultSet(set);
        }
        return rows;
    }

    @Nullable
    @Override
    public String getSql() {
        return sql;
    }
}
