package org.ilu.pv.model;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessTransferFunction implements Serializable {

  @XmlAttribute
  @NotNull
  private Double gainK;

  @NotNull
  @XmlAttribute
  private Double timeConstantT;

  @NotNull
  @XmlAttribute
  private Double delayL;

  public Double getGainK() {
    return gainK;
  }

  public void setGainK(Double gainK) {
    this.gainK = gainK;
  }

  public Double getTimeConstantT() {
    return timeConstantT;
  }

  public void setTimeConstantT(Double timeConstantT) {
    this.timeConstantT = timeConstantT;
  }

  public Double getDelayL() {
    return delayL;
  }

  public void setDelayL(Double delayL) {
    this.delayL = delayL;
  }

  @Override
  public String toString() {
    return "ProcessTransferFunction{" +
        "gainK=" + gainK +
        ", timeConstantT=" + timeConstantT +
        ", delayL=" + delayL +
        '}';
  }
}
