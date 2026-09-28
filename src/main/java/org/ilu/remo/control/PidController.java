package org.ilu.remo.control;

/**
 * Positional PID controller.
 *
 * <ul>
 *   <li>Gains are per second and every step receives the elapsed time, so tuning does not depend on the sample rate.</li>
 *   <li>The derivative acts on the measurement, not the error, so a setpoint change does not cause a derivative kick.</li>
 *   <li>Conditional integration prevents the integral from driving a saturated output farther out (anti-windup).</li>
 *   <li>{@link #initialize} gives a bumpless transfer when taking over from manual mode or after a fault.</li>
 * </ul>
 *
 * <p>Not thread-safe: it is owned by a single control loop.
 */
public final class PidController {

    public record Gains(double kp, double ki, double kd) {
        public Gains {
            requireNonNegative("kp", kp);
            requireNonNegative("ki", ki);
            requireNonNegative("kd", kd);
        }

        private static void requireNonNegative(String name, double value) {
            if (!Double.isFinite(value) || value < 0) {
                throw new IllegalArgumentException(name + " must be a finite, non-negative number, was " + value);
            }
        }
    }

    public record OutputLimits(double min, double max) {
        public OutputLimits {
            if (!Double.isFinite(min) || !Double.isFinite(max) || min >= max) {
                throw new IllegalArgumentException("Output limits need finite min < max, were [" + min + ", " + max + "]");
            }
        }

        public double clamp(double value) {
            return Math.max(min, Math.min(max, value));
        }
    }

    /** DIRECT: more output raises the process value. REVERSE: more output lowers it (e.g. cooling). */
    public enum Direction {
        DIRECT(1),
        REVERSE(-1);

        private final int sign;

        Direction(int sign) {
            this.sign = sign;
        }
    }

    private Gains gains;
    private Direction direction;
    private final OutputLimits limits;

    private double integral;
    private double lastMeasurement;
    private double output;
    private boolean transferPending;

    public PidController(Gains gains, Direction direction, OutputLimits limits) {
        this.gains = gains;
        this.direction = direction;
        this.limits = limits;
        this.output = limits.min();
    }

    /**
     * Computes the next output.
     *
     * @param dtSeconds time elapsed since the previous update or {@link #initialize}
     */
    public double update(double setpoint, double measurement, double dtSeconds) {
        if (!Double.isFinite(dtSeconds) || dtSeconds <= 0) {
            throw new IllegalArgumentException("dtSeconds must be positive and finite, was " + dtSeconds);
        }

        double error = direction.sign * (setpoint - measurement);
        double proportional = gains.kp() * error;
        double derivative = -direction.sign * gains.kd() * (measurement - lastMeasurement) / dtSeconds;
        if (transferPending) {
            // Balance P and D so taking control starts from the output already in effect.
            integral = output - proportional - derivative;
            transferPending = false;
        }

        double candidateIntegral = integral + gains.ki() * error * dtSeconds;
        double candidateOutput = proportional + candidateIntegral + derivative;
        boolean pushesHighSaturation = candidateOutput > limits.max() && error > 0;
        boolean pushesLowSaturation = candidateOutput < limits.min() && error < 0;
        if (!pushesHighSaturation && !pushesLowSaturation) {
            integral = candidateIntegral;
        }

        output = limits.clamp(proportional + integral + derivative);
        lastMeasurement = measurement;

        return output;
    }

    /** Clears accumulated control state while priming the derivative from {@code measurement}. */
    public void reset(double measurement) {
        integral = 0;
        lastMeasurement = measurement;
        output = limits.min();
        transferPending = false;
    }

    /** Continues smoothly from {@code currentOutput}: the next update compensates its P and D contributions. */
    public void initialize(double currentOutput, double measurement) {
        output = limits.clamp(currentOutput);
        lastMeasurement = measurement;
        transferPending = true;
    }

    public Gains gains() {
        return gains;
    }

    public void setGains(Gains gains) {
        this.gains = gains;
    }

    public Direction direction() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public OutputLimits limits() {
        return limits;
    }

    public double output() {
        return output;
    }
}
