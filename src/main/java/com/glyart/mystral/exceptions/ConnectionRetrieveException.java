package com.glyart.mystral.exceptions;

import java.sql.SQLException;

public class ConnectionRetrieveException extends RuntimeException {

    public ConnectionRetrieveException(String msg) {
        super(msg);
    }

    public ConnectionRetrieveException(String msg, SQLException e) {
        super(msg, e);
    }
}
