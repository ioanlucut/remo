package com.remo.core.exceptions;

/**
 * Sql exception class
 */
public class SqlQueriesException extends Exception {

  private static final long serialVersionUID = 2700693591998063033L;

  public SqlQueriesException(String message, Throwable cause) {
    super(message, cause);
  }

  public SqlQueriesException(String message) {
    super(message);
  }

  public SqlQueriesException(Throwable cause) {
    super(cause);
  }
}