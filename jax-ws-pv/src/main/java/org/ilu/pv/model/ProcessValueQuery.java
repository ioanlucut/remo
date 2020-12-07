package org.ilu.pv.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessValueQuery implements Serializable {

    @NotNull
    @XmlAttribute(required = true)
    private Long processId;

    @NotNull
    @XmlAttribute(required = true)
    private Double lastReceivedProcessValue;

    @NotNull
    @XmlAttribute(required = true)
    private Double lastComputedCommand;

    @NotNull
    @Past
    @XmlAttribute(required = true)
    private Date queryDate;

    @NotNull
    @XmlElement
    private ProcessTransferFunction processTransferFunction;

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Double getLastReceivedProcessValue() {
        return lastReceivedProcessValue;
    }

    public void setLastReceivedProcessValue(Double lastReceivedProcessValue) {
        this.lastReceivedProcessValue = lastReceivedProcessValue;
    }

    public Double getLastComputedCommand() {
        return lastComputedCommand;
    }

    public void setLastComputedCommand(Double lastComputedCommand) {
        this.lastComputedCommand = lastComputedCommand;
    }

    public Date getQueryDate() {
        return queryDate;
    }

    public void setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
    }

    public ProcessTransferFunction getProcessTransferFunction() {
        return processTransferFunction;
    }

    public void setProcessTransferFunction(ProcessTransferFunction processTransferFunction) {
        this.processTransferFunction = processTransferFunction;
    }

    @Override
    public String toString() {
        return "ProcessValueQuery{" +
            "processId=" + processId +
            ", lastReceivedProcessValue=" + lastReceivedProcessValue +
            ", lastComputedCommand=" + lastComputedCommand +
            ", queryDate=" + queryDate +
            ", processTransferFunction=" + processTransferFunction +
            '}';
    }
}
