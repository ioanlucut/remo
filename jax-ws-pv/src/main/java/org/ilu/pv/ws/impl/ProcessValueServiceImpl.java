package org.ilu.pv.ws.impl;

import org.apache.log4j.Logger;
import org.ilu.pv.exception.ProcessValueQueryException;
import org.ilu.pv.model.ProcessTransferFunction;
import org.ilu.pv.model.ProcessValueQuery;
import org.ilu.pv.model.ProcessValueQueryFeedback;
import org.ilu.pv.simulation.ProcessValueSimulator;
import org.ilu.pv.validator.ProcessValueServiceValidator;
import org.ilu.pv.violationconverter.ProcessValueViolationConverterService;
import org.ilu.pv.ws.ProcessValueService;

import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

@WebService(endpointInterface = "org.ilu.pv.ws.ProcessValueService")
@HandlerChain(file = "handler-chain.xml")
public class ProcessValueServiceImpl implements ProcessValueService {

  private static final Logger LOGGER = Logger.getLogger(ProcessValueServiceImpl.class);

  /**
   * Process value simulator
   */
  @Inject
  private ProcessValueSimulator processValueSimulator;

  @Inject
  private ProcessValueServiceValidator processValueServiceValidator;

  @Inject
  private ProcessValueViolationConverterService processValueViolationConverterService;

  @Override
  public ProcessValueQueryFeedback query(@NotNull ProcessValueQuery processValueQuery) throws
      ProcessValueQueryException {
    LOGGER.info(String.format("Query received %s at %s", processValueQuery, new Timestamp(new Date().getTime())));

/*        MessageContext requestContext = webServiceContext.getMessageContext();
        Map headers = (Map) requestContext.get(MessageContext.HTTP_REQUEST_HEADERS);

        if (headers != null) {
            List usernameList = (List) headers.get("Username");
            if (usernameList != null && usernameList.size() > 0) {
                LOGGER.info(usernameList.get(0));
            }
            List passwordList = (List) headers.get("Password");
            if (passwordList != null && passwordList.size() > 0) {
                LOGGER.info(passwordList.get(0));
            }
        }*/

    Set<ConstraintViolation<ProcessValueQuery>> violations = processValueServiceValidator.validate(processValueQuery);
    if (!violations.isEmpty()) {
      LOGGER.info(String.format("Constraint violations %s at %s", processValueQuery, violations));
      throw processValueViolationConverterService.convertToSoapFault(violations);
    }

    return buildProcessValueQueryFeedback(processValueQuery);
  }

  private ProcessValueQueryFeedback buildProcessValueQueryFeedback(@NotNull ProcessValueQuery processValueQuery) {
    ProcessValueQueryFeedback processValueQueryFeedback = new ProcessValueQueryFeedback();
    processValueQueryFeedback.setReadDateOfProcessValue(new Date());
    processValueQueryFeedback.setProcessId(processValueQuery.getProcessId());
    processValueQueryFeedback.setReceivedTransferFunction(processValueQuery.getProcessTransferFunction());
    double simulatedProcessValue = buildSimulatedInput(processValueQuery);
    processValueQueryFeedback.setProcessValue(simulatedProcessValue);

    return processValueQueryFeedback;
  }

  /**
   * Process value is simulated
   *
   * @param processValueQuery which contains information about the process and last input and last PID command.
   * @return simulated input after the new PID command is applied.
   */
  private double buildSimulatedInput(@NotNull ProcessValueQuery processValueQuery) {
    ProcessTransferFunction processTransferFunction = processValueQuery.getProcessTransferFunction();
    double lastReceivedProcessValue = processValueQuery.getLastReceivedProcessValue();
    double lastComputedCommand = processValueQuery.getLastComputedCommand();

    return processValueSimulator.getSimulatedInput(processTransferFunction, lastComputedCommand, lastReceivedProcessValue);

  }
}