package org.ilu.cmd.ws;

import org.apache.log4j.Logger;
import org.ilu.cmd.exception.CommandFeedbackException;
import org.ilu.cmd.model.Command;
import org.ilu.cmd.model.CommandFeedback;
import org.ilu.cmd.validator.CommandServiceValidator;
import org.ilu.cmd.violationconverter.CommandViolationConverterService;

import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

@WebService(endpointInterface = "org.ilu.cmd.ws.CommandService")
@HandlerChain(file = "handler-chain.xml")
public class CommandServiceImpl implements CommandService {

  private static final Logger LOGGER = Logger.getLogger(CommandServiceImpl.class);

  @Inject
  private CommandServiceValidator commandServiceValidator;

  @Inject
  private CommandViolationConverterService commandViolationConverterService;

  @Override
  public CommandFeedback submit(@NotNull Command command) throws
      CommandFeedbackException {
    LOGGER.info(String.format("Query received %s at %s", command, new Timestamp(new Date().getTime())));

    if (command == null) {
      throw new CommandFeedbackException("Cannot be null");
    }

    Set<ConstraintViolation<Command>> violations = commandServiceValidator.validate(command);
    if (!violations.isEmpty()) {
      LOGGER.info(String.format("Constraint violations %s at %s", command, violations));
      throw commandViolationConverterService.convertToSoapFault(violations);
    }

    return buildCommandFeedback(command);
  }

  private CommandFeedback buildCommandFeedback(@NotNull Command command) {
    CommandFeedback commandFeedback = new CommandFeedback();
    commandFeedback.setReceivedCommand(command);
    commandFeedback.setActuatorStarted(true);
    commandFeedback.setCommandApplied(true);
    commandFeedback.setReceivedDate(new Date());
    commandFeedback.setSuccessfulReceived(true);
    commandFeedback.setUnwantedDelayUntilCommandApplied(2500);

    return commandFeedback;
  }

}