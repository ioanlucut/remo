
package org.ilu.pv.client;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for processValueQuery complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="processValueQuery">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ws.pv.ilu.org/}processTransferFunction" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="processId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="lastReceivedProcessValue" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="lastComputedCommand" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="queryDate" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processValueQuery", propOrder = {
    "processTransferFunction"
})
public class ProcessValueQuery {

    @XmlElement
    protected ProcessTransferFunction processTransferFunction;
    @XmlAttribute(name = "processId", required = true)
    protected long processId;
    @XmlAttribute(name = "lastReceivedProcessValue", required = true)
    protected double lastReceivedProcessValue;
    @XmlAttribute(name = "lastComputedCommand", required = true)
    protected double lastComputedCommand;
    @XmlAttribute(name = "queryDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar queryDate;

    /**
     * Gets the value of the processTransferFunction property.
     *
     * @return possible object is
     * {@link ProcessTransferFunction }
     */
    public ProcessTransferFunction getProcessTransferFunction() {
        return processTransferFunction;
    }

    /**
     * Sets the value of the processTransferFunction property.
     *
     * @param value allowed object is
     *              {@link ProcessTransferFunction }
     */
    public void setProcessTransferFunction(ProcessTransferFunction value) {
        this.processTransferFunction = value;
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
     * Gets the value of the lastReceivedProcessValue property.
     */
    public double getLastReceivedProcessValue() {
        return lastReceivedProcessValue;
    }

    /**
     * Sets the value of the lastReceivedProcessValue property.
     */
    public void setLastReceivedProcessValue(double value) {
        this.lastReceivedProcessValue = value;
    }

    /**
     * Gets the value of the lastComputedCommand property.
     */
    public double getLastComputedCommand() {
        return lastComputedCommand;
    }

    /**
     * Sets the value of the lastComputedCommand property.
     */
    public void setLastComputedCommand(double value) {
        this.lastComputedCommand = value;
    }

    /**
     * Gets the value of the queryDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getQueryDate() {
        return queryDate;
    }

    /**
     * Sets the value of the queryDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setQueryDate(XMLGregorianCalendar value) {
        this.queryDate = value;
    }

}
