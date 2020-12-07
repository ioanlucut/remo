package org.ilu.pv.violationconverter;

import org.ilu.pv.exception.ProcessValueQueryException;
import org.ilu.pv.model.ProcessValueQuery;

import javax.validation.ConstraintViolation;
import java.util.Set;

public interface ProcessValueViolationConverterService {

  public ProcessValueQueryException convertToSoapFault(Set<ConstraintViolation<ProcessValueQuery>> constraintViolations)
      throws ProcessValueQueryException;
}