package org.ilu.cmd.violationconverter;

import org.ilu.cmd.exception.CommandFeedbackException;
import org.ilu.cmd.model.Command;

import javax.validation.ConstraintViolation;
import java.util.Set;

public interface CommandViolationConverterService {

  public CommandFeedbackException convertToSoapFault(Set<ConstraintViolation<Command>> constraintViolations)
      throws CommandFeedbackException;
}