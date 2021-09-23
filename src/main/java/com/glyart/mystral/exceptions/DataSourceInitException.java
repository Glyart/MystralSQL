package com.glyart.mystral.exceptions;

import com.glyart.mystral.datasource.DataSourceFactory;

/**
 * Describes a problem occurred while trying to create a DataSource.
 * @see DataSourceFactory
 */
public class DataSourceInitException extends RuntimeException {

    public DataSourceInitException(Exception e) {
        super(e);
    }

    /**
     * Construct a new DataSourceInitException with the given message and the offending exception.
     * @param msg the message
     * @param e the exception
     */
    public DataSourceInitException(String msg, Exception e) {
        super(msg, e);
    }

}
