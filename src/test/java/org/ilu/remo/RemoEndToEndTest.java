package org.ilu.remo;

import com.fasterxml.jackson.databind.JsonNode;
import org.ilu.remo.actuator.ActuatorService;
import org.ilu.remo.control.ControlLoop;
import org.ilu.remo.control.PidController;
import org.ilu.remo.controller.ControllerService;
import org.ilu.remo.http.Http;
import org.ilu.remo.plant.FirstOrderPlant;
import org.ilu.remo.sensor.SensorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** All three services on real sockets, with a fast plant so the loop settles in a few seconds. */
@Timeout(30)
class RemoEndToEndTest {

    private ActuatorService actuator;
    private SensorService sensor;
    private ControllerService controller;
    private Http.Client api;

    @BeforeEach
    void startServices() {
        actuator = new ActuatorService(new ActuatorService.Config(0, Duration.ofSeconds(1), 0, 0, 100));
        URI actuatorUri = URI.create("http://localhost:" + actuator.port() + "/");
        sensor = new SensorService(new SensorService.Config(0, actuatorUri, Duration.ofMillis(5),
            new FirstOrderPlant.Parameters(1.5, 0.3, 0)));
        URI sensorUri = URI.create("http://localhost:" + sensor.port() + "/");
        controller = new ControllerService(new ControllerService.Config(0, sensorUri, actuatorUri, Duration.ofMillis(40),
            60, new PidController.Gains(1, 3, 0), new PidController.OutputLimits(0, 100),
            new ControlLoop.Settings(Duration.ofMillis(200), Duration.ofMillis(500), 0)));
        api = new Http.Client(URI.create("http://localhost:" + controller.port() + "/"), Duration.ofSeconds(2));
    }

    @AfterEach
    void stopServices() {
        controller.close();
        sensor.close();
        actuator.close();
    }

    @Test
    void closesTheLoopOverHttpAndFailsSafeWhenTheSensorLinkDrops() throws Exception {
        JsonNode settled = awaitSample(sample -> sample.path("state").asText().equals("RUNNING")
            && Math.abs(sample.path("processValue").asDouble() - 60) < 1.5);
        assertThat(settled.path("sensorOk").asBoolean()).isTrue();

        api.post("api/sensor/faults", new Faults(true, 0, 0), JsonNode.class);

        JsonNode failSafe = awaitSample(sample -> sample.path("state").asText().equals("FAIL_SAFE"));
        assertThat(failSafe.path("processValue").isNull()).isTrue();
        assertThat(failSafe.path("output").asDouble()).isZero();

        api.post("api/sensor/faults", new Faults(false, 0, 0), JsonNode.class);

        awaitSample(sample -> sample.path("state").asText().equals("RUNNING"));
    }

    @Test
    void settingsChangeTheRunningLoop() throws Exception {
        JsonNode settings = api.post("api/settings", Map.of("setpoint", 30, "kp", 1.5), JsonNode.class);

        assertThat(settings.path("setpoint").asDouble()).isEqualTo(30);
        assertThat(settings.path("kp").asDouble()).isEqualTo(1.5);
        assertThat(settings.path("ki").asDouble()).isEqualTo(3);
        awaitSample(sample -> Math.abs(sample.path("processValue").asDouble() - 30) < 1.5);
    }

    @Test
    void rejectsInvalidInputWithTheServiceMessage() {
        assertThatThrownBy(() -> api.post("api/plant", Map.of("gain", 1, "timeConstantSeconds", -1), JsonNode.class))
            .isInstanceOf(Http.RemoteStatus.class)
            .hasMessageContaining("timeConstantSeconds");
        assertThatThrownBy(() -> api.post("api/settings", Map.of("kp", -2), JsonNode.class))
            .isInstanceOf(Http.RemoteStatus.class)
            .hasMessageContaining("kp");
    }

    @Test
    void streamsSamplesToTheBrowser() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + controller.port() + "/events")).build();
        HttpResponse<InputStream> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (BufferedReader events = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line = events.readLine();

            assertThat(response.headers().firstValue("Content-Type")).contains("text/event-stream");
            assertThat(line).startsWith("data: {").contains("\"state\"");
        }
    }

    private record Faults(boolean down, long latencyMillis, double dropRate) {
    }

    private JsonNode awaitSample(Predicate<JsonNode> condition) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
        while (System.nanoTime() < deadline) {
            JsonNode history = api.get("api/history", JsonNode.class);
            if (!history.isEmpty() && condition.test(history.get(history.size() - 1))) {
                return history.get(history.size() - 1);
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Condition not reached; last samples: " + api.get("api/history", JsonNode.class));
    }
}
