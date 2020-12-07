package org.ilu.cmd.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Command implements Serializable {

  /**
   * Related process ID
   */
  @NotNull
  @XmlAttribute(required = true)
  private long processId;

  @NotNull
  @XmlAttribute(required = true)
  private double commandPercent;

  @NotNull
  @Past
  @XmlAttribute(required = true)
  private Date sentDate;

  /**
   * Maximum time allowed to execute command
   */
  @NotNull
  @XmlAttribute(required = true)
  private Date maximumTimeToExecuteCommand;

  /**
   * If the command cannot be applied, tells if actuator should perform emergency STOP or not.
   */
  @NotNull
  @XmlAttribute(required = true)
  private boolean emergencyStopIfNotApplied;

  /**
   * If this is set as true, then command is ignored if takes more than maximum time allowed.
   */
  @NotNull
  @XmlAttribute(required = true)
  private boolean ignoreCommandIfTakesMoreThanMaxTimeAllowed;

  public long getProcessId() {
    return processId;
  }

  public void setProcessId(long processId) {
    this.processId = processId;
  }

  public double getCommandPercent() {
    return commandPercent;
  }

  public void setCommandPercent(double commandPercent) {
    this.commandPercent = commandPercent;
  }

  public Date getSentDate() {
    return sentDate;
  }

  public void setSentDate(Date sentDate) {
    this.sentDate = sentDate;
  }

  public Date getMaximumTimeToExecuteCommand() {
    return maximumTimeToExecuteCommand;
  }

  public void setMaximumTimeToExecuteCommand(Date maximumTimeToExecuteCommand) {
    this.maximumTimeToExecuteCommand = maximumTimeToExecuteCommand;
  }

  public boolean isEmergencyStopIfNotApplied() {
    return emergencyStopIfNotApplied;
  }

  public void setEmergencyStopIfNotApplied(boolean emergencyStopIfNotApplied) {
    this.emergencyStopIfNotApplied = emergencyStopIfNotApplied;
  }

  public boolean isIgnoreCommandIfTakesMoreThanMaxTimeAllowed() {
    return ignoreCommandIfTakesMoreThanMaxTimeAllowed;
  }

  public void setIgnoreCommandIfTakesMoreThanMaxTimeAllowed(boolean ignoreCommandIfTakesMoreThanMaxTimeAllowed) {
    this.ignoreCommandIfTakesMoreThanMaxTimeAllowed = ignoreCommandIfTakesMoreThanMaxTimeAllowed;
  }
}
