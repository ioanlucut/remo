package org.ilu.handler;

import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Interceptor so that the SOAP requests and responses are logged.
 *
 * @author i.lucut
 */
public class SOAPRequestResponseInterceptor implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOGGER = Logger.getLogger(SOAPRequestResponseInterceptor.class);

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    logToSystemOut(context);

    return true;
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    logToSystemOut(context);

    return true;
  }

  private void logToSystemOut(SOAPMessageContext context) {
    SOAPMessage soapMessage = context.getMessage();
    Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

    LOGGER.info(outboundProperty ? "RESPONSE: " : "REQUEST: ");
    logHeaders(context, outboundProperty);
    logMessage(soapMessage);
  }

  private void logHeaders(SOAPMessageContext context, boolean outbound) {
    try {
      BindingProvider bindingProvider = (BindingProvider) context.get("com.sun.xml.internal.ws.client.handle");
      if (bindingProvider != null) {
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        if (requestContext != null) {
          LOGGER.info(outbound ? requestContext.get(MessageContext.HTTP_REQUEST_HEADERS)
              : requestContext.get(MessageContext.HTTP_RESPONSE_HEADERS));
        }
      }
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
  }

  private void logMessage(SOAPMessage soapMessage) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      soapMessage.writeTo(baos);
      LOGGER.info("*****************************************************");
      LOGGER.info("\n" + new XmlPrettyFormatter().format(baos.toString()));
      LOGGER.info("*****************************************************");
    } catch (Exception ex) {
      LOGGER.error(ex);
    }
  }

  @Override
  public void close(MessageContext context) {
  }

  @Override
  public Set<QName> getHeaders() {
    return Collections.emptySet();
  }
}