package org.ilu.cmd.validator.impl;

import org.ilu.cmd.exception.CommandFeedbackException;
import org.ilu.cmd.model.Command;
import org.ilu.cmd.validator.CommandServiceValidator;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

public class CommandServiceValidatorImpl implements CommandServiceValidator {

  @Inject
  private Validator validator;

  @Override
  public Set<ConstraintViolation<Command>> validate(Command command)
      throws CommandFeedbackException {

    return validator.validate(command);
  }
}
