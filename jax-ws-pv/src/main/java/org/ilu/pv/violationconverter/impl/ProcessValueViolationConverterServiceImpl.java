package org.ilu.pv.violationconverter.impl;

import org.ilu.pv.exception.ProcessValueQueryException;
import org.ilu.pv.model.ProcessValueQuery;
import org.ilu.pv.violationconverter.ProcessValueViolationConverterService;

import javax.validation.ConstraintViolation;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.util.Set;

public class ProcessValueViolationConverterServiceImpl implements ProcessValueViolationConverterService {

    @Override
    public ProcessValueQueryException convertToSoapFault(Set<ConstraintViolation<ProcessValueQuery>> constraintViolations)
        throws ProcessValueQueryException {

        try {
            SOAPFault fault = SOAPFactory.newInstance()
                .createFault("The provided process value query is invalid", SOAPConstants.SOAP_SENDER_FAULT);
            Detail detail = fault.addDetail();
            QName entryName = new QName("http://gizmos.com/orders/", "order", "PO");
            DetailEntry entry = detail.addDetailEntry(entryName);

            for (ConstraintViolation<ProcessValueQuery> violation : constraintViolations) {
                String formattedViolation = String.format("%s %s", violation.getPropertyPath(), violation.getMessage());
                entry.addTextNode(formattedViolation);
            }
            return new ProcessValueQueryException(fault);
        } catch (SOAPException ex) {
            ex.printStackTrace();
            return new ProcessValueQueryException("Internal error");
        }
    }
}
