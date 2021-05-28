package com.glyart.mystral.sql.impl;

import com.glyart.mystral.database.PreparedStatementUtils;
import com.glyart.mystral.sql.PreparedStatementSetter;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DefaultSetterUnknownType implements PreparedStatementSetter {

    @Nullable
    private final Object[] params;

    public DefaultSetterUnknownType(@Nullable Object[] params) {
        this.params = params;
    }

    @Override
    public void setValues(@NotNull PreparedStatement ps) throws SQLException {
        Preconditions.checkNotNull(ps, "PreparedStatement cannot be null.");
        if (params == null)
            return;

        for (int i = 0; i < params.length; i++)
            PreparedStatementUtils.setValue(ps, i + 1, Integer.MIN_VALUE, params[i]);
    }
}
