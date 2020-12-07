package org.ilu.handler;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;

public class HeaderHandlerResolver implements HandlerResolver {

  /**
   * The interface is set up with raw types.
   */
  @SuppressWarnings("rawtypes")
  public List<Handler> getHandlerChain(PortInfo portInfo) {

    // Responsible for logging the SOAP request.
    SOAPRequestResponseInterceptor requestResponseLogger = new SOAPRequestResponseInterceptor();
    List<Handler> handlerChain = new ArrayList<>();
    handlerChain.add(requestResponseLogger);

    return handlerChain;
  }
}