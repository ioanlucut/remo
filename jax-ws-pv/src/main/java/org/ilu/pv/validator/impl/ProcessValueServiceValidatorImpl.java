package org.ilu.pv.validator.impl;

import org.ilu.pv.exception.ProcessValueQueryException;
import org.ilu.pv.model.ProcessValueQuery;
import org.ilu.pv.validator.ProcessValueServiceValidator;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

public class ProcessValueServiceValidatorImpl implements ProcessValueServiceValidator {

  @Inject
  private Validator validator;

  @Override
  public Set<ConstraintViolation<ProcessValueQuery>> validate(ProcessValueQuery processValueQuery)
      throws ProcessValueQueryException {

    return validator.validate(processValueQuery);
  }
}
