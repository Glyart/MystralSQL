package com.glyart.mystral.sql.impl;

import com.google.common.base.Preconditions;
import com.glyart.mystral.sql.ResultSetExtractor;
import com.glyart.mystral.sql.SqlProvider;
import com.glyart.mystral.sql.StatementFunction;
import com.glyart.mystral.datasource.DataSourceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryStatementFunction<T> implements StatementFunction<T>, SqlProvider {

    @NotNull
    private final ResultSetExtractor<T> extractor;

    @Nullable
    private final String sql;

    public QueryStatementFunction(@NotNull ResultSetExtractor<T> extractor, @Nullable String sql) {
        Preconditions.checkNotNull(extractor, "ResultSetExtractor cannot be null.");
        this.extractor = extractor;
        this.sql = sql;
    }

    @Override
    public @Nullable T apply(@NotNull Statement statement) throws SQLException {
        ResultSet resultSet = null;
        T result;
        try {
            resultSet = statement.executeQuery(sql);
            result = extractor.extractData(resultSet);
        } finally {
            DataSourceUtils.closeResultSet(resultSet);
        }
        return result;
    }

    @Nullable
    @Override
    public String getSql() {
        return sql;
    }
}
