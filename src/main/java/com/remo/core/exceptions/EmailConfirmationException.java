package com.remo.core.exceptions;

public class EmailConfirmationException extends Exception {

  private static final long serialVersionUID = 3191414045402168756L;

  public EmailConfirmationException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailConfirmationException(String message) {
    super(message);
  }

  public EmailConfirmationException(Throwable cause) {
    super(cause);
  }
}
