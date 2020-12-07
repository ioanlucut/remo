package com.remo.core.control.pid;

import com.remo.core.control.pid.model.*;

import java.io.Serializable;

public class PidControllerServiceImpl implements PidControllerService, Serializable {

    private static final long serialVersionUID = 1L;

    private Pid pid;

    /**
     * PV
     */
    private volatile double input;

    /**
     * CMD
     */
    private volatile double output;

    /**
     * SP
     */
    private volatile double setPoint;

    private volatile long lastTime;
    private volatile double iTerm, lastInput;

    private volatile long sampleTime = 100;

    private volatile double outMin, outMax;
    private volatile boolean inAuto;

    private volatile long sampleCount = 0;

    /**
     * Computes output per given input and setPoint.
     *
     * @return true if output was properly computed, false otherwise.
     * <p/>
     * Returns false if PID is not in auto MODE.
     */
    @Override
    public boolean compute() {
        if (!this.inAuto || !this.getPid().getPidParams().isPidParamsDefined()) {
            return false;
        }

        long now = System.currentTimeMillis();
        long timeChange = (now - this.lastTime);

        if (pidIsAllowedToCompute(timeChange)) {
            ++this.sampleCount;

            /* compute all the working error variables */
            double error = this.setPoint - this.input;
            this.iTerm += (this.pid.getPidParams().getKi() * error);
            if (this.iTerm > this.outMax) {
                this.iTerm = this.outMax;
            } else if (this.iTerm < this.outMin) {
                this.iTerm = this.outMin;
            }
            double dInput = (input - lastInput);

            /* compute PID Output */
            double output = pid.getPidParams().getKp() * error + iTerm - pid.getPidParams().getKd() * dInput;

            if (output > outMax) {
                output = outMax;
            } else if (output < outMin) {
                output = outMin;
            }
            this.output = output;

            /* Remember some variables for next time */
            this.lastInput = this.input;
            this.lastTime = now;

            return true;
        } else {
            return false;
        }
    }

    private boolean pidIsAllowedToCompute(long timeChange) {
        return timeChange >= this.sampleTime;
    }

    /*
     * setPID(...)************************************************************* This
     * function allows the controller's dynamic performance to be adjusted. it's called
     * automatically from the constructor, but tunings can also be adjusted on the fly
     * during normal operation *******************************
     * *********************************************
     */
    public void setPID(PidParams pidParams) {
        if (pidParams.getKp() < 0 || pidParams.getKi() < 0 || pidParams.getKd() < 0) {
            return;
        }

        double SampleTimeInSec = ((double) sampleTime) / 1000;
        this.pid.getPidParams().setKp(pidParams.getKp());
        this.pid.getPidParams().setKi(pidParams.getKi() * SampleTimeInSec);
        this.pid.getPidParams().setKd(pidParams.getKd() / SampleTimeInSec);

        if (pid.getPidDirection() == PidDirection.REVERSE) {
            this.pid.getPidParams().setKp(0 - pidParams.getKp());
            this.pid.getPidParams().setKi(0 - pidParams.getKi());
            this.pid.getPidParams().setKd(0 - pidParams.getKd());
        }
    }

    /*
     * setSampleTimeEntry(...) ********************************************************* sets
     * the period, in Milliseconds, at which the calculation is performed ************
     * ****************************************************************
     */
    public void setSampleTime(long sampleTime) {
        if (sampleTime > 0) {
            double ratio = (double) sampleTime / (double) this.sampleTime;
            this.pid.getPidParams().setKi(pid.getPidParams().getKi() * ratio);
            this.pid.getPidParams().setKd(pid.getPidParams().getKd() / ratio);
            this.sampleTime = sampleTime;
        }
    }

    /*
     * setOutputRange(...)**************************************************** This
     * function will be used far more often than SetInputLimits. while the input to the
     * controller will generally be in the 0-1023 range (which is the default already,)
     * the output will be a little different. maybe they'll be doing a time window and
     * will need 0-8000 or something. or maybe they'll want to clamp it from 0-125. who
     * knows. at any rate, that can all be done here.
     * ************************************************************************
     */
    public void setOutputRange(PidOutputRange range) {
        if (range.getMinRange() >= range.getMaxRange()) {
            return;
        }
        outMin = range.getMinRange();
        outMax = range.getMaxRange();

        if (inAuto) {
            if (output > outMax) {
                output = outMax;
            } else if (output < outMin) {
                output = outMin;
            }

            if (iTerm > outMax) {
                iTerm = outMax;
            } else if (iTerm < outMin) {
                iTerm = outMin;
            }
        }
    }

    /*
     * Initialize()************************************************************** ** does
     * all the things that need to happen to ensure a bumpless transfer from manual to
     * automatic mode. ********************************************
     * ********************************
     */
    public void init() {
        iTerm = output;
        lastInput = input;
        if (iTerm > outMax) {
            iTerm = outMax;
        } else if (iTerm < outMin) {
            iTerm = outMin;
        }

        lastTime = System.currentTimeMillis() - sampleTime;
    }

    /*
     * SetControllerDirection(...)*********************************************** ** The
     * PID will either be connected to a DIRECT acting process (+Output leads to +Input)
     * or a REVERSE acting process(+Output leads to -Input.) we need to know which one,
     * because otherwise we may increase the output when we should be decreasing. This is
     * called from the constructor. *************
     * ***************************************************************
     */
    public void setPidDirection(PidDirection pidDirection) {
        if (inAuto && pidDirection != this.pid.getPidDirection()) {
            this.pid.getPidParams().setKp(0 - pid.getPidParams().getKp());
            this.pid.getPidParams().setKi(0 - pid.getPidParams().getKi());
            this.pid.getPidParams().setKd(0 - pid.getPidParams().getKd());
        }
        this.pid.setPidDirection(pidDirection);
    }

    /*
     * SetMode(...)************************************************************** **
     * Allows the controller Mode to be set to manual (0) or Automatic (non-zero) when the
     * transition from manual to auto occurs, the controller is automatically initialized
     * ********************************************** ******************************
     */
    public void setPidMode(PidMode pidMode) {
        boolean newPidModeSettings = (pidMode == PidMode.AUTO);
        /* we just went from manual to auto */
        if (newPidModeSettings == !inAuto) {
            init();
        }
        inAuto = newPidModeSettings;
    }

    public void invert() {
        setPidDirection(PidDirection.REVERSE);
    }

    public void direct() {
        setPidDirection(PidDirection.DIRECT);
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public double getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }

    public PidMode getPidMode() {
        return inAuto ? PidMode.AUTO : PidMode.MANUAL;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public boolean isInAuto() {
        return inAuto;
    }

    public void setInAuto(boolean inAuto) {
        this.inAuto = inAuto;
    }

    public double getOutMax() {
        return outMax;
    }

    public void setOutMax(double outMax) {
        this.outMax = outMax;
    }

    public double getOutMin() {
        return outMin;
    }

    public void setOutMin(double outMin) {
        this.outMin = outMin;
    }

    public long getSampleTime() {
        return sampleTime;
    }

    public double getLastInput() {
        return lastInput;
    }

    public void setLastInput(double lastInput) {
        this.lastInput = lastInput;
    }

    public double getiTerm() {
        return iTerm;
    }

    public void setiTerm(double iTerm) {
        this.iTerm = iTerm;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public double getInput() {
        return input;
    }

    public void setInput(double input) {
        this.input = input;
    }

    public Pid getPid() {
        return pid;
    }

    public void setPid(Pid pid) {
        this.pid = pid;
        this.setOutputRange(pid.getPidOutputRange());
    }
}
