
package org.ilu.pv.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for query complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="query">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://ws.pv.ilu.org/}processValueQuery" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "query", propOrder = {
    "arg0"
})
public class Query {

  protected ProcessValueQuery arg0;

  /**
   * Gets the value of the arg0 property.
   *
   * @return possible object is
   * {@link ProcessValueQuery }
   */
  public ProcessValueQuery getArg0() {
    return arg0;
  }

  /**
   * Sets the value of the arg0 property.
   *
   * @param value allowed object is
   *              {@link ProcessValueQuery }
   */
  public void setArg0(ProcessValueQuery value) {
    this.arg0 = value;
  }

}
