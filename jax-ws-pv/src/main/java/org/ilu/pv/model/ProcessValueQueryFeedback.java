package org.ilu.pv.model;

import javax.enterprise.context.RequestScoped;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Date;

@RequestScoped
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessValueQueryFeedback implements Serializable {

    @XmlAttribute(required = true)
    private Long processId;

    @XmlAttribute(required = true)
    private Double processValue;

    @XmlAttribute(required = true)
    private Date readDateOfProcessValue;

    @XmlElement(required = true, namespace = "")
    private ProcessTransferFunction receivedTransferFunction;

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Double getProcessValue() {
        return processValue;
    }

    public void setProcessValue(Double processValue) {
        this.processValue = processValue;
    }

    public Date getReadDateOfProcessValue() {
        return readDateOfProcessValue;
    }

    public void setReadDateOfProcessValue(Date readDateOfProcessValue) {
        this.readDateOfProcessValue = readDateOfProcessValue;
    }

    public ProcessTransferFunction getReceivedTransferFunction() {
        return receivedTransferFunction;
    }

    public void setReceivedTransferFunction(ProcessTransferFunction receivedTransferFunction) {
        this.receivedTransferFunction = receivedTransferFunction;
    }

    @Override
    public String toString() {
        return "ProcessValueQueryFeedback{" +
            "processId=" + processId +
            ", processValue=" + processValue +
            ", readDateOfProcessValue=" + readDateOfProcessValue +
            ", receivedTransferFunction=" + receivedTransferFunction +
            '}';
    }
}
