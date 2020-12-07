
package org.ilu.pv.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for processTransferFunction complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="processTransferFunction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="gainK" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="timeConstantT" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="delayL" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processTransferFunction")
public class ProcessTransferFunction {

  @XmlAttribute(name = "gainK")
  protected Double gainK;
  @XmlAttribute(name = "timeConstantT")
  protected Double timeConstantT;
  @XmlAttribute(name = "delayL")
  protected Double delayL;

  /**
   * Gets the value of the gainK property.
   *
   * @return possible object is
   * {@link Double }
   */
  public Double getGainK() {
    return gainK;
  }

  /**
   * Sets the value of the gainK property.
   *
   * @param value allowed object is
   *              {@link Double }
   */
  public void setGainK(Double value) {
    this.gainK = value;
  }

  /**
   * Gets the value of the timeConstantT property.
   *
   * @return possible object is
   * {@link Double }
   */
  public Double getTimeConstantT() {
    return timeConstantT;
  }

  /**
   * Sets the value of the timeConstantT property.
   *
   * @param value allowed object is
   *              {@link Double }
   */
  public void setTimeConstantT(Double value) {
    this.timeConstantT = value;
  }

  /**
   * Gets the value of the delayL property.
   *
   * @return possible object is
   * {@link Double }
   */
  public Double getDelayL() {
    return delayL;
  }

  /**
   * Sets the value of the delayL property.
   *
   * @param value allowed object is
   *              {@link Double }
   */
  public void setDelayL(Double value) {
    this.delayL = value;
  }

}
