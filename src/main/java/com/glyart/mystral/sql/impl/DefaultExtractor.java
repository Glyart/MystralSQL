package com.glyart.mystral.sql.impl;

import com.glyart.mystral.sql.ResultSetExtractor;
import com.glyart.mystral.sql.ResultSetRowMapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefaultExtractor<T> implements ResultSetExtractor<List<T>> {

    @NotNull
    private final ResultSetRowMapper<T> mapper;
    private final int limit;

    public DefaultExtractor(@NotNull ResultSetRowMapper<T> mapper) {
        this(mapper, 0);
    }

    public DefaultExtractor(@NotNull ResultSetRowMapper<T> mapper, int limit) {
        Preconditions.checkNotNull(mapper, "ResultSetRowMapper cannot be null.");
        this.mapper = mapper;
        this.limit = limit;
    }

    @Nullable
    @Override
    public List<T> extractData(@NotNull ResultSet rs) throws SQLException {
        List<T> list = limit == 0 ? new ArrayList<>() : new ArrayList<>(Math.abs(limit));
        int rowNum = 0;
        while (rs.next())
            list.add(mapper.map(rs, rowNum++));

        return list;
    }
}