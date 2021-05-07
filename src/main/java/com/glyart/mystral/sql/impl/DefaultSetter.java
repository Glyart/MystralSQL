package com.glyart.mystral.sql.impl;

import com.google.common.base.Preconditions;
import com.glyart.mystral.sql.PreparedStatementSetter;
import com.glyart.mystral.database.PreparedStatementUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DefaultSetter implements PreparedStatementSetter {

    @Nullable
    private final Object[] params;
    private final int[] sqlTypes;

    public DefaultSetter(Object[] params, int[] sqlTypes) {
        this.params = params;
        this.sqlTypes = sqlTypes;
    }

    @Override
    public void setValues(@NotNull PreparedStatement ps) throws SQLException {
        Preconditions.checkNotNull(ps, "PreparedStatement cannot be null.");
        if (params == null)
            return;

        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            PreparedStatementUtils.setValue(ps, i + 1, sqlTypes[i], param);
        }
    }
}
