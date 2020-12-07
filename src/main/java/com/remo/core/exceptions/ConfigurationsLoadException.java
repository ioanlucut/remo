package com.remo.core.exceptions;

public class ConfigurationsLoadException extends Exception {

  private static final long serialVersionUID = 3120049045521339424L;

  public ConfigurationsLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationsLoadException(String message) {
    super(message);
  }

  public ConfigurationsLoadException(Throwable cause) {
    super(cause);
  }
}