package org.ilu.remo.control;

import org.ilu.remo.control.ControlLoop.Sample;
import org.ilu.remo.control.ControlLoop.State;
import org.ilu.remo.control.PidController.Direction;
import org.ilu.remo.control.PidController.Gains;
import org.ilu.remo.control.PidController.OutputLimits;
import org.ilu.remo.plant.FirstOrderPlant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** The loop against an in-memory plant, with links that fail on demand and a clock the test controls. */
class ControlLoopTest {

    private static final long SAMPLE_NANOS = Duration.ofMillis(100).toNanos();

    private final FirstOrderPlant plant = new FirstOrderPlant(new FirstOrderPlant.Parameters(1.5, 8, 1), 0.1, 0);
    private long nowNanos;
    private boolean sensorUp = true;
    private boolean actuatorUp = true;
    private long readingAgeMillis;
    private double applied;
    private ControlLoop loop;

    @BeforeEach
    void setUp() {
        ControlLoop.Sensor sensor = () -> {
            if (!sensorUp) {
                throw new IOException("sensor down");
            }
            return new ControlLoop.Reading(plant.value(), readingAgeMillis);
        };
        ControlLoop.Actuator actuator = command -> {
            if (!actuatorUp) {
                throw new IOException("actuator down");
            }
            applied = command;
        };
        loop = new ControlLoop(
            new PidController(new Gains(2.5, 0.3, 0), Direction.DIRECT, new OutputLimits(0, 100)),
            sensor, actuator,
            new ControlLoop.Settings(Duration.ofMillis(500), Duration.ofSeconds(3), 0),
            () -> nowNanos);
        loop.setSetpoint(60);
    }

    @Test
    void bringsTheProcessToTheSetpoint() {
        Sample last = runSeconds(120);

        assertThat(last.state()).isEqualTo(State.RUNNING);
        assertThat(plant.value()).isCloseTo(60, within(0.5));
    }

    @Test
    void holdsTheLastOutputWhenTheSensorIsLostInsteadOfReadingZero() {
        runSeconds(120);
        double settledOutput = applied;

        sensorUp = false;
        Sample sample = runSeconds(1);

        assertThat(sample.state()).isEqualTo(State.HOLDING);
        assertThat(sample.processValue()).isNull();
        assertThat(applied).isEqualTo(settledOutput);
    }

    @Test
    void goesToTheSafeOutputWhenTheSensorStaysLost() {
        runSeconds(120);

        sensorUp = false;
        Sample sample = runSeconds(4);

        assertThat(sample.state()).isEqualTo(State.FAIL_SAFE);
        assertThat(applied).isZero();
    }

    @Test
    void treatsAStaleReadingAsLost() {
        runSeconds(10);

        readingAgeMillis = 2_000;
        Sample sample = runSeconds(0.2);

        assertThat(sample.sensorOk()).isFalse();
        assertThat(sample.state()).isEqualTo(State.HOLDING);
    }

    @Test
    void resumesWithoutABumpWhenTheSensorComesBack() {
        runSeconds(120);
        double settledOutput = applied;
        sensorUp = false;
        runSeconds(1);

        sensorUp = true;
        Sample recovered = tick();

        assertThat(recovered.state()).isEqualTo(State.RUNNING);
        assertThat(applied).isEqualTo(settledOutput);
    }

    @Test
    void doesNotWindUpWhileTheActuatorIsUnreachable() {
        runSeconds(120);
        double settledOutput = applied;
        loop.setSetpoint(120);

        actuatorUp = false;
        Sample sample = runSeconds(30);
        actuatorUp = true;
        tick();

        assertThat(sample.state()).isEqualTo(State.HOLDING);
        assertThat(sample.actuatorOk()).isFalse();
        assertThat(applied).isEqualTo(settledOutput);
    }

    @Test
    void manualModeSendsTheOperatorOutputAndHandsBackWithoutABump() {
        runSeconds(60);

        loop.setManual(true, 30);
        Sample manual = runSeconds(1);
        loop.setManual(false, 30);
        tick();

        assertThat(manual.state()).isEqualTo(State.MANUAL);
        assertThat(applied).isEqualTo(30);
    }

    private Sample runSeconds(double seconds) {
        Sample last = null;
        long ticks = Math.round(seconds * 1e9 / SAMPLE_NANOS);
        for (long i = 0; i < ticks; i++) {
            last = tick();
        }

        return last;
    }

    private Sample tick() {
        nowNanos += SAMPLE_NANOS;
        plant.step(applied);

        return loop.tick();
    }
}
