package com.glyart.mystral.exceptions;

import java.sql.SQLException;

/**
 * Exception thrown when a result was not of the expected size,
 * for example when expecting a single row but getting 0 or more than 1 rows.
 */
public class IncorrectDataSizeException extends RuntimeException {

    private final int expectedSize;
    private final int actualSize;

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     * @param expectedSize the expected result size
     */
    public IncorrectDataSizeException(int expectedSize) {
        super("Incorrect result size: expected " + expectedSize);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     * @param expectedSize the expected result size
     * @param actualSize the actual result size (or -1 if unknown)
     */
    public IncorrectDataSizeException(int expectedSize, int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     * @param msg the detail message
     * @param expectedSize the expected result size
     */
    public IncorrectDataSizeException(String msg, int expectedSize) {
        super(msg);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     * @param msg the detail message
     * @param expectedSize the expected result size
     * @param ex the wrapped exception
     */
    public IncorrectDataSizeException(String msg, int expectedSize, SQLException ex) {
        super(msg, ex);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     * @param msg the detail message
     * @param expectedSize the expected result size
     * @param actualSize the actual result size (or -1 if unknown)
     */
    public IncorrectDataSizeException(String msg, int expectedSize, int actualSize) {
        super(msg);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     * @param msg the detail message
     * @param expectedSize the expected result size
     * @param actualSize the actual result size (or -1 if unknown)
     * @param ex the wrapped exception
     */
    public IncorrectDataSizeException(String msg, int expectedSize, int actualSize, SQLException ex) {
        super(msg, ex);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * Return the expected result size.
     */
    public int getExpectedSize() {
        return this.expectedSize;
    }

    /**
     * Return the actual result size (or -1 if unknown).
     */
    public int getActualSize() {
        return this.actualSize;
    }
}
