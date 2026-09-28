package org.ilu.remo.plant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class FirstOrderPlantTest {

    private static final double STEP = 0.01;

    @Test
    void stepResponseReaches63PercentAfterOneTimeConstant() {
        FirstOrderPlant plant = new FirstOrderPlant(new FirstOrderPlant.Parameters(2, 5, 0), STEP, 0);

        double value = run(plant, 10, 5);

        assertThat(value).isCloseTo(20 * (1 - Math.exp(-1)), within(0.01));
    }

    @Test
    void settlesAtGainTimesInput() {
        FirstOrderPlant plant = new FirstOrderPlant(new FirstOrderPlant.Parameters(1.5, 2, 0), STEP, 0);

        assertThat(run(plant, 40, 30)).isCloseTo(60, within(1e-3));
    }

    @Test
    void deadTimeDelaysTheResponse() {
        FirstOrderPlant plant = new FirstOrderPlant(new FirstOrderPlant.Parameters(1, 1, 2), STEP, 0);

        assertThat(run(plant, 50, 1.99)).isZero();
        assertThat(run(plant, 50, 0.5)).isGreaterThan(0);
    }

    @Test
    void parametersCanChangeWhileRunning() {
        FirstOrderPlant plant = new FirstOrderPlant(new FirstOrderPlant.Parameters(1, 1, 0), STEP, 0);
        run(plant, 10, 20);

        plant.setParameters(new FirstOrderPlant.Parameters(3, 1, 0.5));

        assertThat(run(plant, 10, 20)).isCloseTo(30, within(1e-3));
    }

    @Test
    void rejectsPhysicallyMeaninglessParameters() {
        assertThatThrownBy(() -> new FirstOrderPlant.Parameters(1, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FirstOrderPlant.Parameters(1, -3, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FirstOrderPlant.Parameters(1, 1, -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FirstOrderPlant.Parameters(Double.NaN, 1, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static double run(FirstOrderPlant plant, double input, double seconds) {
        long steps = Math.round(seconds / STEP);
        for (long i = 0; i < steps; i++) {
            plant.step(input);
        }

        return plant.value();
    }
}
