package com.glyart.mystral.sql.impl;

import com.google.common.base.Preconditions;
import com.glyart.mystral.sql.BatchSetter;
import com.glyart.mystral.database.PreparedStatementUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DefaultBatchSetter implements BatchSetter {

    @NotNull
    private final List<Object[]> batchParams;
    private final int[] sqlTypes;

    public DefaultBatchSetter(@NotNull List<Object[]> batchParams, int[] sqlTypes) {
        this.batchParams = batchParams;
        this.sqlTypes = sqlTypes;
    }

    @Override
    public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
        Preconditions.checkNotNull(ps, "PreparedStatement cannot be null.");
        Object[] objects = batchParams.get(i);
        for (int pos = 0; pos < objects.length; pos++) {
            Object object = objects[pos];
            PreparedStatementUtils.setValue(ps, pos + 1, sqlTypes[pos], object);
        }
    }

    @Override
    public int getBatchSize() {
        return batchParams.size();
    }
}
