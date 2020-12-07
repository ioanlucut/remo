
package org.ilu.cmd.client;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for command complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="command">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="processId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="commandPercent" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="sentDate" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="maximumTimeToExecuteCommand" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="emergencyStopIfNotApplied" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="ignoreCommandIfTakesMoreThanMaxTimeAllowed" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "command")
public class Command {

    @XmlAttribute(name = "processId", required = true)
    protected long processId;
    @XmlAttribute(name = "commandPercent", required = true)
    protected double commandPercent;
    @XmlAttribute(name = "sentDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar sentDate;
    @XmlAttribute(name = "maximumTimeToExecuteCommand", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar maximumTimeToExecuteCommand;
    @XmlAttribute(name = "emergencyStopIfNotApplied", required = true)
    protected boolean emergencyStopIfNotApplied;
    @XmlAttribute(name = "ignoreCommandIfTakesMoreThanMaxTimeAllowed", required = true)
    protected boolean ignoreCommandIfTakesMoreThanMaxTimeAllowed;

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
     * Gets the value of the commandPercent property.
     */
    public double getCommandPercent() {
        return commandPercent;
    }

    /**
     * Sets the value of the commandPercent property.
     */
    public void setCommandPercent(double value) {
        this.commandPercent = value;
    }

    /**
     * Gets the value of the sentDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getSentDate() {
        return sentDate;
    }

    /**
     * Sets the value of the sentDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setSentDate(XMLGregorianCalendar value) {
        this.sentDate = value;
    }

    /**
     * Gets the value of the maximumTimeToExecuteCommand property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getMaximumTimeToExecuteCommand() {
        return maximumTimeToExecuteCommand;
    }

    /**
     * Sets the value of the maximumTimeToExecuteCommand property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setMaximumTimeToExecuteCommand(XMLGregorianCalendar value) {
        this.maximumTimeToExecuteCommand = value;
    }

    /**
     * Gets the value of the emergencyStopIfNotApplied property.
     */
    public boolean isEmergencyStopIfNotApplied() {
        return emergencyStopIfNotApplied;
    }

    /**
     * Sets the value of the emergencyStopIfNotApplied property.
     */
    public void setEmergencyStopIfNotApplied(boolean value) {
        this.emergencyStopIfNotApplied = value;
    }

    /**
     * Gets the value of the ignoreCommandIfTakesMoreThanMaxTimeAllowed property.
     */
    public boolean isIgnoreCommandIfTakesMoreThanMaxTimeAllowed() {
        return ignoreCommandIfTakesMoreThanMaxTimeAllowed;
    }

    /**
     * Sets the value of the ignoreCommandIfTakesMoreThanMaxTimeAllowed property.
     */
    public void setIgnoreCommandIfTakesMoreThanMaxTimeAllowed(boolean value) {
        this.ignoreCommandIfTakesMoreThanMaxTimeAllowed = value;
    }

}
