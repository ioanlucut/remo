package com.remo.core.control.tf.model;

import com.remo.core.shiro.models.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

import static com.remo.core.control.tf.model.TransferFunction.TF_LIST;
import static com.remo.core.control.tf.model.TransferFunction.TF_LIST_OF_USER;

@Entity
@NamedQueries({
    @NamedQuery(name = TF_LIST, query = "SELECT b FROM TransferFunction b"),
    @NamedQuery(name = TF_LIST_OF_USER, query = "SELECT b FROM TransferFunction b WHERE b.user = :user")})
@Table(name = "tf")
public class TransferFunction implements Serializable {

    public static final String TF_LIST = "Tf.list";
    public static final String TF_LIST_OF_USER = "Tf.user";

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date tfTimestamp;

    private Double gainK;
    private Double timeConstantT;
    private Double delayL;

    @PrePersist
    protected void onCreate() {
        tfTimestamp = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTfTimestamp() {
        return tfTimestamp;
    }

    public void setTfTimestamp(Date tfTimestamp) {
        this.tfTimestamp = tfTimestamp;
    }

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransferFunction that = (TransferFunction) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
