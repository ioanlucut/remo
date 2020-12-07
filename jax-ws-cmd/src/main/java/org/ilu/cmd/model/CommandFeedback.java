package org.ilu.cmd.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CommandFeedback implements Serializable {

  @XmlAttribute(required = true)
  private boolean successfulReceived;

  @XmlAttribute(required = true)
  private boolean commandApplied;

  @XmlAttribute(required = true)
  private boolean actuatorStarted;

  @XmlAttribute(required = true)
  private Date receivedDate;

  /**
   * In millis
   */
  @XmlAttribute(required = true)
  private long unwantedDelayUntilCommandApplied;

  /**
   * Represents the command that was received - acts as a TRACE to check if the command sent was proper.
   */
  @XmlElement(required = true)
  private Command receivedCommand;

  public boolean isSuccessfulReceived() {
    return successfulReceived;
  }

  public void setSuccessfulReceived(boolean successfulReceived) {
    this.successfulReceived = successfulReceived;
  }

  public boolean isCommandApplied() {
    return commandApplied;
  }

  public void setCommandApplied(boolean commandApplied) {
    this.commandApplied = commandApplied;
  }

  public boolean isActuatorStarted() {
    return actuatorStarted;
  }

  public void setActuatorStarted(boolean actuatorStarted) {
    this.actuatorStarted = actuatorStarted;
  }

  public Date getReceivedDate() {
    return receivedDate;
  }

  public void setReceivedDate(Date receivedDate) {
    this.receivedDate = receivedDate;
  }

  public long getUnwantedDelayUntilCommandApplied() {
    return unwantedDelayUntilCommandApplied;
  }

  public void setUnwantedDelayUntilCommandApplied(long unwantedDelayUntilCommandApplied) {
    this.unwantedDelayUntilCommandApplied = unwantedDelayUntilCommandApplied;
  }

  public Command getReceivedCommand() {
    return receivedCommand;
  }

  public void setReceivedCommand(Command receivedCommand) {
    this.receivedCommand = receivedCommand;
  }

  @Override
  public String toString() {
    return "CommandFeedback{" +
        "successfulReceived=" + successfulReceived +
        ", commandApplied=" + commandApplied +
        ", actuatorStarted=" + actuatorStarted +
        ", receivedDate=" + receivedDate +
        ", unwantedDelayUntilCommandApplied=" + unwantedDelayUntilCommandApplied +
        ", receivedCommand=" + receivedCommand +
        '}';
  }
}
