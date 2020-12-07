package com.remo.core.control.pid.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "pid_params")
public class PidParams {

    @Id
    @GeneratedValue
    private Long id;

    // * (P)roportional Tuning Parameter
    @NotNull
    @Column(nullable = false)
    private Double kp;
    // * (I)ntegral Tuning Parameter

    @NotNull
    @Column(nullable = false)
    private Double ki;
    // * (D)erivative Tuning Parameter

    @NotNull
    @Column(nullable = false)
    private Double kd;

    public Double getKp() {
        return kp;
    }

    public void setKp(Double kp) {
        this.kp = kp;
    }

    public Double getKi() {
        return ki;
    }

    public void setKi(Double ki) {
        this.ki = ki;
    }

    public Double getKd() {
        return kd;
    }

    public void setKd(Double kd) {
        this.kd = kd;
    }

    public boolean isPidParamsDefined() {
        return kp != null && ki != null && kd != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PidParams pidParams = (PidParams) o;

        if (kd != null ? !kd.equals(pidParams.kd) : pidParams.kd != null) {
            return false;
        }
        if (ki != null ? !ki.equals(pidParams.ki) : pidParams.ki != null) {
            return false;
        }
        if (kp != null ? !kp.equals(pidParams.kp) : pidParams.kp != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = kp != null ? kp.hashCode() : 0;
        result = 31 * result + (ki != null ? ki.hashCode() : 0);
        result = 31 * result + (kd != null ? kd.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PidParams{" +
            "kp=" + kp +
            ", ki=" + ki +
            ", kd=" + kd +
            '}';
    }
}
