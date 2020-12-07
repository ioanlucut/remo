package com.remo.core.control.gui;

import java.io.Serializable;

public class SeriesSample implements Serializable {

  /**
   * Sample time entry (seconds)
   */
  private long sampleTimeEntry;

  /**
   * Received remote
   */
  private double receivedProcessValue;

  /**
   * Sent command output
   */
  private double sentCommandOutput;

  /**
   * Received command output
   */
  private double receivedCommandOutput;

  /**
   * Given setPoint of this sample.
   */
  private double setPoint;

  public double getReceivedProcessValue() {
    return receivedProcessValue;
  }

  public void setReceivedProcessValue(double receivedProcessValue) {
    this.receivedProcessValue = receivedProcessValue;
  }

  public double getSentCommandOutput() {
    return sentCommandOutput;
  }

  public void setSentCommandOutput(double sentCommandOutput) {
    this.sentCommandOutput = sentCommandOutput;
  }

  public double getReceivedCommandOutput() {
    return receivedCommandOutput;
  }

  public void setReceivedCommandOutput(double receivedCommandOutput) {
    this.receivedCommandOutput = receivedCommandOutput;
  }

  public double getSetPoint() {
    return setPoint;
  }

  public void setSetPoint(double setPoint) {
    this.setPoint = setPoint;
  }

  public long getSampleTimeEntry() {
    return sampleTimeEntry;
  }

  public void setSampleTimeEntry(long sampleTimeEntry) {
    this.sampleTimeEntry = sampleTimeEntry;
  }
}
