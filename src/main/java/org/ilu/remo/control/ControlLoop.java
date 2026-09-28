package org.ilu.remo.control;

import java.io.IOException;
import java.time.Duration;
import java.util.function.LongSupplier;

/**
 * One PID loop closed over a remote sensor and a remote actuator, either of which can fail.
 *
 * <p>Fault policy:
 * <ul>
 *   <li><b>Sensor unreachable or stale:</b> never treat the gap as a reading. Keep sending the last output
 *       ({@link State#HOLDING}); if the sensor stays lost for longer than {@code holdFor}, send the safe output
 *       ({@link State#FAIL_SAFE}).</li>
 *   <li><b>Actuator unreachable:</b> stop integrating, because the process is no longer following the output.
 *       Keep retrying the last output that was actually applied.</li>
 *   <li><b>Recovery:</b> re-initialise the PID from the output in effect, so control resumes without a bump.</li>
 * </ul>
 *
 * <p>{@link #tick()} is called by one scheduler thread; the setters may be called from request threads.
 */
public final class ControlLoop {

    public interface Sensor {
        Reading read() throws IOException;
    }

    public interface Actuator {
        void apply(double command) throws IOException;
    }

    /** @param ageMillis how long ago the sensor measured this value, by the sensor's own clock */
    public record Reading(double value, long ageMillis) {
    }

    public record Settings(Duration staleAfter, Duration holdFor, double safeOutput) {
    }

    public enum State {
        /** PID in control. */
        RUNNING,
        /** Operator sets the output directly. */
        MANUAL,
        /** A link is down; the last output is held. */
        HOLDING,
        /** The sensor has been lost for too long; the safe output is sent. */
        FAIL_SAFE
    }

    /** @param processValue {@code null} when no valid reading was available */
    public record Sample(double elapsedSeconds, double setpoint, Double processValue, double output,
                         boolean sensorOk, boolean actuatorOk, State state) {
    }

    private final PidController pid;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Settings settings;
    private final LongSupplier nanoClock;
    private final long startNanos;

    private double setpoint;
    private boolean manual;
    private double manualOutput;

    private long lastTickNanos;
    private long sensorLostSinceNanos = -1;
    private boolean needsInitialize = true;
    private double appliedOutput;

    public ControlLoop(PidController pid, Sensor sensor, Actuator actuator, Settings settings, LongSupplier nanoClock) {
        this.pid = pid;
        this.sensor = sensor;
        this.actuator = actuator;
        this.settings = settings;
        this.nanoClock = nanoClock;
        this.startNanos = nanoClock.getAsLong();
        this.lastTickNanos = startNanos;
        this.appliedOutput = pid.limits().clamp(settings.safeOutput());
    }

    public synchronized Sample tick() {
        long now = nanoClock.getAsLong();
        double dtSeconds = (now - lastTickNanos) / 1e9;
        lastTickNanos = now;

        Double processValue = readSensor();
        double command;
        State state;

        if (processValue == null) {
            if (sensorLostSinceNanos < 0) {
                sensorLostSinceNanos = now;
            }
            boolean holdExpired = now - sensorLostSinceNanos >= settings.holdFor().toNanos();
            command = holdExpired ? pid.limits().clamp(settings.safeOutput()) : appliedOutput;
            state = holdExpired ? State.FAIL_SAFE : State.HOLDING;
            needsInitialize = true;
        } else {
            sensorLostSinceNanos = -1;
            if (manual) {
                command = manualOutput;
                state = State.MANUAL;
                needsInitialize = true;
            } else if (needsInitialize || dtSeconds <= 0) {
                pid.initialize(appliedOutput, processValue);
                command = appliedOutput;
                state = State.RUNNING;
                needsInitialize = false;
            } else {
                command = pid.update(setpoint, processValue, dtSeconds);
                state = State.RUNNING;
            }
        }

        boolean actuatorOk = applyToActuator(command);
        if (actuatorOk) {
            appliedOutput = command;
        } else {
            needsInitialize = true;
            if (state == State.RUNNING) {
                state = State.HOLDING;
            }
        }

        double elapsedSeconds = (now - startNanos) / 1e9;
        return new Sample(elapsedSeconds, setpoint, processValue, appliedOutput, processValue != null, actuatorOk, state);
    }

    private Double readSensor() {
        try {
            Reading reading = sensor.read();
            boolean stale = reading.ageMillis() > settings.staleAfter().toMillis();

            return stale || !Double.isFinite(reading.value()) ? null : reading.value();
        } catch (IOException ex) {
            return null;
        }
    }

    private boolean applyToActuator(double command) {
        try {
            actuator.apply(command);

            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public synchronized double setpoint() {
        return setpoint;
    }

    public synchronized void setSetpoint(double setpoint) {
        if (!Double.isFinite(setpoint)) {
            throw new IllegalArgumentException("setpoint must be finite, was " + setpoint);
        }
        this.setpoint = setpoint;
    }

    public synchronized boolean manual() {
        return manual;
    }

    public synchronized double manualOutput() {
        return manualOutput;
    }

    /** Switching back to automatic continues from the manual output without a bump. */
    public synchronized void setManual(boolean manual, double manualOutput) {
        this.manual = manual;
        this.manualOutput = pid.limits().clamp(manualOutput);
    }

    public synchronized PidController.Gains gains() {
        return pid.gains();
    }

    public synchronized void setGains(PidController.Gains gains) {
        pid.setGains(gains);
    }

    public synchronized PidController.Direction direction() {
        return pid.direction();
    }

    public synchronized void setDirection(PidController.Direction direction) {
        if (direction != pid.direction()) {
            pid.setDirection(direction);
            needsInitialize = true;
        }
    }

    public PidController.OutputLimits limits() {
        return pid.limits();
    }
}
