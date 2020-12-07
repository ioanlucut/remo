package org.ilu.pv.validator;

import org.ilu.pv.exception.ProcessValueQueryException;
import org.ilu.pv.model.ProcessValueQuery;

import javax.validation.ConstraintViolation;
import java.util.Set;

public interface ProcessValueServiceValidator {

  public Set<ConstraintViolation<ProcessValueQuery>> validate(ProcessValueQuery processValueQuery)
      throws ProcessValueQueryException;
}