package org.ilu.pv.exception;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebFault;

@WebFault
public class ProcessValueQueryException extends Exception {

  private SOAPFault fault;

  public ProcessValueQueryException(SOAPFault fault) {
    super(fault.getFaultString());
    this.fault = fault;
  }

  public ProcessValueQueryException(String message) {
    super(message);
  }

  public ProcessValueQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessValueQueryException(Throwable cause) {
    super(cause);
  }

}