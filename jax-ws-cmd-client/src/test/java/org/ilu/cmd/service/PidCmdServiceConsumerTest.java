package org.ilu.cmd.service;

import org.apache.log4j.Logger;
import org.ilu.cmd.client.Command;
import org.ilu.cmd.client.CommandFeedback;
import org.ilu.cmd.client.CommandFeedbackException_Exception;
import org.ilu.cmd.client.CommandService;
import org.ilu.common.XMLCalendarToDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PidCmdServiceConsumerTest {

  private static final Logger LOGGER = Logger.getLogger(PidCmdServiceConsumerTest.class);

  @Test(expected = CommandFeedbackException_Exception.class)
  public void testOne() throws CommandFeedbackException_Exception {
    CommandService cmdService = new PidCmdServiceConsumer().getService();
    CommandFeedback response = cmdService.submit(null);
    LOGGER.info(response);
  }

  @Test(expected = CommandFeedbackException_Exception.class)
  public void testTwo() throws CommandFeedbackException_Exception {
    CommandService cmdService = new PidCmdServiceConsumer().getService();
    Command command = new Command();
    CommandFeedback response = cmdService.submit(command);
    LOGGER.info(response);
  }

  @Test
  public void testThree() throws CommandFeedbackException_Exception {
    CommandService cmdService = new PidCmdServiceConsumer().getService();
    Command command = new Command();
    command.setCommandPercent(40.5);
    command.setSentDate(XMLCalendarToDate.XMLCalendarToDate());
    command.setMaximumTimeToExecuteCommand(XMLCalendarToDate.XMLCalendarToDate());
    CommandFeedback response = cmdService.submit(command);
    assertEquals(response.getReceivedCommand().getCommandPercent(), command.getCommandPercent(), 0.0);
    LOGGER.info(response);
  }
}
