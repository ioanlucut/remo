package com.remo.core.exceptions;

public class LoadPropertiesException extends Exception {

  private static final long serialVersionUID = -5092825564264023050L;

  public LoadPropertiesException(String message, Throwable cause) {
    super(message, cause);
  }

  public LoadPropertiesException(String message) {
    super(message);
  }

  public LoadPropertiesException(Throwable cause) {
    super(cause);
  }
}
