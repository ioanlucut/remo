package org.ilu.remo.control;

import org.ilu.remo.control.PidController.Direction;
import org.ilu.remo.control.PidController.Gains;
import org.ilu.remo.control.PidController.OutputLimits;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class PidControllerTest {

    private static final OutputLimits LIMITS = new OutputLimits(0, 100);

    @Test
    void proportionalOutputIsGainTimesError() {
        PidController pid = new PidController(new Gains(2, 0, 0), Direction.DIRECT, LIMITS);
        pid.initialize(0, 40);

        assertThat(pid.update(50, 40, 0.1)).isEqualTo(20);
    }

    @Test
    void integralDependsOnElapsedTimeNotOnSampleRate() {
        PidController fast = new PidController(new Gains(0, 1, 0), Direction.DIRECT, LIMITS);
        PidController slow = new PidController(new Gains(0, 1, 0), Direction.DIRECT, LIMITS);
        fast.initialize(0, 0);
        slow.initialize(0, 0);

        double fastOutput = 0;
        for (int i = 0; i < 10; i++) {
            fastOutput = fast.update(10, 0, 0.1);
        }
        double slowOutput = slow.update(10, 0, 1.0);

        assertThat(fastOutput).isCloseTo(10, within(1e-9));
        assertThat(slowOutput).isCloseTo(10, within(1e-9));
    }

    @Test
    void outputIsClampedToLimits() {
        PidController pid = new PidController(new Gains(100, 0, 0), Direction.DIRECT, LIMITS);
        pid.initialize(0, 0);

        assertThat(pid.update(50, 0, 0.1)).isEqualTo(100);
        assertThat(pid.update(-50, 0, 0.1)).isEqualTo(0);
    }

    @Test
    void integralDoesNotWindUpWhileSaturated() {
        PidController pid = new PidController(new Gains(1, 5, 0), Direction.DIRECT, LIMITS);
        pid.initialize(0, 0);
        for (int i = 0; i < 1_000; i++) {
            pid.update(1_000, 0, 0.1);
        }

        // Once the error reverses, the output must leave saturation right away instead of unwinding for minutes.
        double output = pid.update(0, 20, 0.1);

        assertThat(output).isLessThan(100);
    }

    @Test
    void reverseDirectionRaisesOutputWhenProcessIsAboveSetpoint() {
        PidController pid = new PidController(new Gains(2, 0, 0), Direction.REVERSE, LIMITS);
        pid.initialize(0, 60);

        assertThat(pid.update(50, 60, 0.1)).isEqualTo(20);
    }

    @Test
    void setpointStepCausesNoDerivativeKick() {
        PidController pid = new PidController(new Gains(0, 0, 10), Direction.DIRECT, LIMITS);
        pid.initialize(50, 40);

        assertThat(pid.update(90, 40, 0.1)).isEqualTo(50);
    }

    @Test
    void derivativeOpposesAMovingMeasurement() {
        PidController pid = new PidController(new Gains(0, 0, 1), Direction.DIRECT, new OutputLimits(-100, 100));
        pid.initialize(0, 40);

        assertThat(pid.update(50, 41, 0.1)).isCloseTo(-10, within(1e-9));
    }

    @Test
    void initializeGivesABumplessTransfer() {
        PidController pid = new PidController(new Gains(3, 1, 0.5), Direction.DIRECT, LIMITS);
        pid.initialize(42, 50);

        assertThat(pid.update(50, 50, 0.1)).isEqualTo(42);
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThatThrownBy(() -> new Gains(-1, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Gains(1, Double.NaN, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutputLimits(10, 10)).isInstanceOf(IllegalArgumentException.class);

        PidController pid = new PidController(new Gains(1, 1, 1), Direction.DIRECT, LIMITS);
        assertThatThrownBy(() -> pid.update(1, 0, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
