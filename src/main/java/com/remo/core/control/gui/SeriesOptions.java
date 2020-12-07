package com.remo.core.control.gui;

import java.io.Serializable;

public class SeriesOptions implements Serializable {

  /**
   * How many samples should be shown from the current moment in the past.
   */
  private long samplesToShow = 100L;

  public long getSamplesToShow() {
    return samplesToShow;
  }

  public void setSamplesToShow(long samplesToShow) {
    this.samplesToShow = samplesToShow;
  }

  @Override
  public String toString() {
    return "SeriesOptions{" +
        "samplesToShow=" + samplesToShow +
        '}';
  }
}
