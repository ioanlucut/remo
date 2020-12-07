
package org.ilu.cmd.client;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for commandFeedback complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="commandFeedback">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="receivedCommand" type="{http://ws.cmd.ilu.org/}command"/>
 *       &lt;/sequence>
 *       &lt;attribute name="successfulReceived" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="commandApplied" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="actuatorStarted" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="receivedDate" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="unwantedDelayUntilCommandApplied" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "commandFeedback", propOrder = {
    "receivedCommand"
})
public class CommandFeedback {

    @XmlElement(required = true)
    protected Command receivedCommand;
    @XmlAttribute(name = "successfulReceived", required = true)
    protected boolean successfulReceived;
    @XmlAttribute(name = "commandApplied", required = true)
    protected boolean commandApplied;
    @XmlAttribute(name = "actuatorStarted", required = true)
    protected boolean actuatorStarted;
    @XmlAttribute(name = "receivedDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar receivedDate;
    @XmlAttribute(name = "unwantedDelayUntilCommandApplied", required = true)
    protected long unwantedDelayUntilCommandApplied;

    /**
     * Gets the value of the receivedCommand property.
     *
     * @return possible object is
     * {@link Command }
     */
    public Command getReceivedCommand() {
        return receivedCommand;
    }

    /**
     * Sets the value of the receivedCommand property.
     *
     * @param value allowed object is
     *              {@link Command }
     */
    public void setReceivedCommand(Command value) {
        this.receivedCommand = value;
    }

    /**
     * Gets the value of the successfulReceived property.
     */
    public boolean isSuccessfulReceived() {
        return successfulReceived;
    }

    /**
     * Sets the value of the successfulReceived property.
     */
    public void setSuccessfulReceived(boolean value) {
        this.successfulReceived = value;
    }

    /**
     * Gets the value of the commandApplied property.
     */
    public boolean isCommandApplied() {
        return commandApplied;
    }

    /**
     * Sets the value of the commandApplied property.
     */
    public void setCommandApplied(boolean value) {
        this.commandApplied = value;
    }

    /**
     * Gets the value of the actuatorStarted property.
     */
    public boolean isActuatorStarted() {
        return actuatorStarted;
    }

    /**
     * Sets the value of the actuatorStarted property.
     */
    public void setActuatorStarted(boolean value) {
        this.actuatorStarted = value;
    }

    /**
     * Gets the value of the receivedDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getReceivedDate() {
        return receivedDate;
    }

    /**
     * Sets the value of the receivedDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setReceivedDate(XMLGregorianCalendar value) {
        this.receivedDate = value;
    }

    /**
     * Gets the value of the unwantedDelayUntilCommandApplied property.
     */
    public long getUnwantedDelayUntilCommandApplied() {
        return unwantedDelayUntilCommandApplied;
    }

    /**
     * Sets the value of the unwantedDelayUntilCommandApplied property.
     */
    public void setUnwantedDelayUntilCommandApplied(long value) {
        this.unwantedDelayUntilCommandApplied = value;
    }

}
