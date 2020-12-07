package org.ilu.cmd.validator;

import org.ilu.cmd.exception.CommandFeedbackException;
import org.ilu.cmd.model.Command;

import javax.validation.ConstraintViolation;
import java.util.Set;

public interface CommandServiceValidator {

  public Set<ConstraintViolation<Command>> validate(Command command) throws CommandFeedbackException;
}