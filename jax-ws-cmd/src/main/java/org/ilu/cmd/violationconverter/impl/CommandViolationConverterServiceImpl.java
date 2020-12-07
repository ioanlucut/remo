package org.ilu.cmd.violationconverter.impl;

import org.ilu.cmd.exception.CommandFeedbackException;
import org.ilu.cmd.model.Command;
import org.ilu.cmd.violationconverter.CommandViolationConverterService;

import javax.validation.ConstraintViolation;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.util.Set;

public class CommandViolationConverterServiceImpl implements CommandViolationConverterService {

    @Override
    public CommandFeedbackException convertToSoapFault(Set<ConstraintViolation<Command>> constraintViolations)
        throws CommandFeedbackException {

        try {
            SOAPFault fault = SOAPFactory.newInstance()
                .createFault("The provided process value query is invalid", SOAPConstants.SOAP_SENDER_FAULT);
            Detail detail = fault.addDetail();
            QName entryName = new QName("http://gizmos.com/orders/", "order", "PO");
            DetailEntry entry = detail.addDetailEntry(entryName);

            for (ConstraintViolation<Command> violation : constraintViolations) {
                String formattedViolation = String.format("%s %s", violation.getPropertyPath(), violation.getMessage());
                entry.addTextNode(formattedViolation);
            }
            return new CommandFeedbackException(fault);
        } catch (SOAPException ex) {
            ex.printStackTrace();
            return new CommandFeedbackException("Internal error");
        }
    }
}
