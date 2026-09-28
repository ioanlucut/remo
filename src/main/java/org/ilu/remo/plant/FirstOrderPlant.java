package org.ilu.remo.plant;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * First-order process with dead time: {@code G(s) = K · e^(-L·s) / (T·s + 1)}.
 *
 * <p>Discretised exactly for a zero-order-hold input: {@code y[k+1] = a·y[k] + K·(1 - a)·u[k - d]} with
 * {@code a = e^(-dt/T)} and {@code d = round(L / dt)} steps of delay.
 *
 * <p>Not thread-safe.
 */
public final class FirstOrderPlant {

    /**
     * @param gain                K, the steady-state change in output per unit of input
     * @param timeConstantSeconds T, the time to reach 63.2% of a step response
     * @param deadTimeSeconds     L, the transport delay before the input has any effect
     */
    public record Parameters(double gain, double timeConstantSeconds, double deadTimeSeconds) {
        public Parameters {
            if (!Double.isFinite(gain)) {
                throw new IllegalArgumentException("gain must be finite, was " + gain);
            }
            if (!Double.isFinite(timeConstantSeconds) || timeConstantSeconds <= 0) {
                throw new IllegalArgumentException("timeConstantSeconds must be positive, was " + timeConstantSeconds);
            }
            if (!Double.isFinite(deadTimeSeconds) || deadTimeSeconds < 0) {
                throw new IllegalArgumentException("deadTimeSeconds must be zero or positive, was " + deadTimeSeconds);
            }
        }
    }

    private final double stepSeconds;
    private final Deque<Double> delayLine = new ArrayDeque<>();

    private Parameters parameters;
    private double decay;
    private double value;

    public FirstOrderPlant(Parameters parameters, double stepSeconds, double initialValue) {
        if (!(stepSeconds > 0)) {
            throw new IllegalArgumentException("stepSeconds must be positive, was " + stepSeconds);
        }
        this.stepSeconds = stepSeconds;
        this.value = initialValue;
        setParameters(parameters);
    }

    /** Advances the process by one step with the given input and returns the new output. */
    public double step(double input) {
        delayLine.addLast(input);
        double delayedInput = delayLine.removeFirst();
        value = decay * value + parameters.gain() * (1 - decay) * delayedInput;

        return value;
    }

    /** Changes the process on the fly. Inputs already in flight stay in the delay line. */
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
        this.decay = Math.exp(-stepSeconds / parameters.timeConstantSeconds());

        int delaySteps = (int) Math.round(parameters.deadTimeSeconds() / stepSeconds);
        double oldestInput = delayLine.isEmpty() ? 0.0 : delayLine.peekFirst();
        while (delayLine.size() < delaySteps) {
            delayLine.addFirst(oldestInput);
        }
        while (delayLine.size() > delaySteps) {
            delayLine.removeFirst();
        }
    }

    public Parameters parameters() {
        return parameters;
    }

    public double value() {
        return value;
    }
}
