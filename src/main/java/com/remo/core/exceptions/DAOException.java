package com.remo.core.exceptions;

public class DAOException extends Exception {

  private static final long serialVersionUID = -5092825564264023050L;

  public DAOException(String message, Throwable cause) {
    super(message, cause);
  }

  public DAOException(String message) {
    super(message);
  }

  public DAOException(Throwable cause) {
    super(cause);
  }
}
