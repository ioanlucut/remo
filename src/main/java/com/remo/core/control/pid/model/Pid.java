package com.remo.core.control.pid.model;

import com.remo.core.shiro.models.User;
import org.apache.log4j.Logger;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

import static com.remo.core.control.pid.model.Pid.BID_LIST;
import static com.remo.core.control.pid.model.Pid.PIDS_LIST_OF_USER;

@Entity
@NamedQueries({
    @NamedQuery(name = BID_LIST, query = "SELECT b FROM Pid b"),
    @NamedQuery(name = PIDS_LIST_OF_USER, query = "SELECT b FROM Pid b WHERE b.user = :user")})
@Table(name = "pid")
public class Pid implements Serializable {

    public final static Logger log = Logger.getLogger(Pid.class.getCanonicalName());

    public static final String BID_LIST = "Pid.list";
    public static final String PIDS_LIST_OF_USER = "Pid.user.user";

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
    private Date pidTimestamp;

    /**
     * Controller settings with DIRECT by default
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private PidDirection pidDirection = PidDirection.DIRECT;

    /**
     * Pid MODE with AUTO by default
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private PidMode pidMode = PidMode.AUTO;

    /**
     * Contains details about PID settings parameters.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pid_params_id")
    private PidParams pidParams = new PidParams();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pid_output_range_id")
    private PidOutputRange pidOutputRange = new PidOutputRange();

    @PrePersist
    protected void onCreate() {
        pidTimestamp = new Date();
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

    public Date getPidTimestamp() {
        return pidTimestamp;
    }

    public void setPidTimestamp(Date pidTimestamp) {
        this.pidTimestamp = pidTimestamp;
    }

    public PidDirection getPidDirection() {
        return pidDirection;
    }

    public void setPidDirection(PidDirection pidDirection) {
        this.pidDirection = pidDirection;
    }

    public PidMode getPidMode() {
        return pidMode;
    }

    public void setPidMode(PidMode pidMode) {
        this.pidMode = pidMode;
    }

    public PidParams getPidParams() {
        return pidParams;
    }

    public void setPidParams(PidParams pidParams) {
        this.pidParams = pidParams;
    }

    public PidOutputRange getPidOutputRange() {
        return pidOutputRange;
    }

    public void setPidOutputRange(PidOutputRange pidOutputRange) {
        this.pidOutputRange = pidOutputRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pid pid = (Pid) o;

        if (id != null ? !id.equals(pid.id) : pid.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
