package org.ilu.cmd.exception;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebFault;

@WebFault
public class CommandFeedbackException extends Exception {

  private SOAPFault fault;

  public CommandFeedbackException(SOAPFault fault) {
    super(fault.getFaultString());
    this.fault = fault;
  }

  public CommandFeedbackException(String message) {
    super(message);
  }

  public CommandFeedbackException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandFeedbackException(Throwable cause) {
    super(cause);
  }

  public CommandFeedbackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}