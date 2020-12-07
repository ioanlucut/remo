
package org.ilu.pv.client;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for processValueQueryFeedback complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="processValueQueryFeedback">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="receivedTransferFunction" type="{http://ws.pv.ilu.org/}processTransferFunction"/>
 *       &lt;/sequence>
 *       &lt;attribute name="processId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="processValue" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="readDateOfProcessValue" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processValueQueryFeedback", propOrder = {
    "receivedTransferFunction"
})
public class ProcessValueQueryFeedback {

    @XmlElement(required = true)
    protected ProcessTransferFunction receivedTransferFunction;
    @XmlAttribute(name = "processId", required = true)
    protected long processId;
    @XmlAttribute(name = "processValue", required = true)
    protected double processValue;
    @XmlAttribute(name = "readDateOfProcessValue", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar readDateOfProcessValue;

    /**
     * Gets the value of the receivedTransferFunction property.
     *
     * @return possible object is
     * {@link ProcessTransferFunction }
     */
    public ProcessTransferFunction getReceivedTransferFunction() {
        return receivedTransferFunction;
    }

    /**
     * Sets the value of the receivedTransferFunction property.
     *
     * @param value allowed object is
     *              {@link ProcessTransferFunction }
     */
    public void setReceivedTransferFunction(ProcessTransferFunction value) {
        this.receivedTransferFunction = value;
    }

    /**
     * Gets the value of the processId property.
     */
    public long getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     */
    public void setProcessId(long value) {
        this.processId = value;
    }

    /**
     * Gets the value of the processValue property.
     */
    public double getProcessValue() {
        return processValue;
    }

    /**
     * Sets the value of the processValue property.
     */
    public void setProcessValue(double value) {
        this.processValue = value;
    }

    /**
     * Gets the value of the readDateOfProcessValue property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getReadDateOfProcessValue() {
        return readDateOfProcessValue;
    }

    /**
     * Sets the value of the readDateOfProcessValue property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setReadDateOfProcessValue(XMLGregorianCalendar value) {
        this.readDateOfProcessValue = value;
    }

}
