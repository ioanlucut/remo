package org.ilu.handler;

import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;
import java.util.TreeSet;

public class SecurityOathClientHeaderHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOGGER = Logger.getLogger(SecurityOathClientHeaderHandler.class);
    public static final String USERNAME = "ilu";
    public static final String PASSWORD = "ilu";

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty) {
            SOAPMessage message = smc.getMessage();
            tryToBuildSecurityOathHeader(message);
        }

        return outboundProperty;
    }

    private void tryToBuildSecurityOathHeader(SOAPMessage message) {
        try {
            buildOathSecurityHeader(message);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void buildOathSecurityHeader(SOAPMessage message) throws SOAPException {
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        SOAPHeader header = envelope.getHeader();
        if (header == null) {
            header = envelope.addHeader();
        }

        SOAPElement security = header.addChildElement("Security", "wsse",
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

        SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
        usernameToken.addAttribute(new QName("xmlns:wsu"),
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

        SOAPElement username = usernameToken.addChildElement("Username", "wsse");
        username.addTextNode(USERNAME);
        SOAPElement password = usernameToken.addChildElement("Password", "wsse");
        password.addTextNode(PASSWORD);
    }

    public Set<QName> getHeaders() {
        return new TreeSet<>();
    }

    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    public void close(MessageContext context) {
    }
}
