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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SecurityOathServerHeaderHandler implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOGGER = Logger.getLogger(SOAPRequestResponseInterceptor.class);

  public static final String USERNAME = "ilu";
  public static final String PASSWORD = "ilu";

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
          Map headers = (Map) requestContext.get(MessageContext.HTTP_REQUEST_HEADERS);
          Object response = requestContext.get(MessageContext.HTTP_RESPONSE_HEADERS);

          if (!outbound) {
            if (headers != null) {
              List usernameList = (List) headers.get("Username");
              if (usernameList != null && usernameList.size() > 0) {
                LOGGER.info(usernameList.get(0));
              }
              List passwordList = (List) headers.get("Password");
              if (passwordList != null && passwordList.size() > 0) {
                LOGGER.info(passwordList.get(0));
              }
            } else {
              LOGGER.info(outbound ? headers : response);
            }
          }

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
