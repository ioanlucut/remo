package org.ilu.remo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import org.ilu.remo.actuator.ActuatorService;
import org.ilu.remo.control.ControlLoop;
import org.ilu.remo.control.PidController;
import org.ilu.remo.http.Http;
import org.ilu.remo.sensor.SensorService;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * The PID controller: closes the loop over the remote sensor and actuator, and serves the dashboard.
 *
 * <ul>
 *   <li>{@code GET /}: the dashboard; {@code GET /events}: live samples as server-sent events</li>
 *   <li>{@code GET /api/settings}, {@code POST /api/settings}: setpoint, gains, direction, mode, demo</li>
 *   <li>{@code GET /api/history}: the recent samples</li>
 *   <li>{@code GET|POST /api/sensor/faults}, {@code /api/actuator/faults}, {@code /api/plant}: passed to the
 *       services</li>
 * </ul>
 */
public final class ControllerService implements AutoCloseable {

    public record Config(int port, URI sensorUri, URI actuatorUri, Duration sampleTime, double initialSetpoint,
                         PidController.Gains gains, PidController.OutputLimits limits, ControlLoop.Settings loop) {
        public static Config defaults(int port, URI sensorUri, URI actuatorUri) {
            return new Config(port, sensorUri, actuatorUri, Duration.ofMillis(100), 60,
                new PidController.Gains(2.5, 0.3, 0),
                new PidController.OutputLimits(0, 100),
                new ControlLoop.Settings(Duration.ofMillis(500), Duration.ofSeconds(3), 0));
        }
    }

    /** Every field is optional; only the ones present are changed. */
    public record SettingsUpdate(Double setpoint, Double kp, Double ki, Double kd,
                                 PidController.Direction direction, Boolean manual, Double manualOutput,
                                 Boolean demo) {
    }

    public record Settings(double setpoint, double kp, double ki, double kd, PidController.Direction direction,
                           boolean manual, double manualOutput, boolean demo, double outputMin, double outputMax,
                           long sampleMillis) {
    }

    private static final int HISTORY_SIZE = 600;
    private static final long DEMO_SETPOINT_PERIOD_NANOS = TimeUnit.SECONDS.toNanos(20);

    private final Config config;
    private final ControlLoop loop;
    private final EventStream events = new EventStream();
    private final Deque<ControlLoop.Sample> history = new ArrayDeque<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform()
        .name("control-loop")
        .daemon()
        .factory());
    private final HttpServer server;

    private volatile boolean demo;
    private long lastDemoChangeNanos;

    public ControllerService(Config config) {
        this.config = config;

        // Both remote calls must fit in one sample period; a slower answer counts as a lost link.
        Duration callTimeout = config.sampleTime().multipliedBy(2).dividedBy(5);
        Http.Client sensor = new Http.Client(config.sensorUri(), callTimeout);
        Http.Client actuator = new Http.Client(config.actuatorUri(), callTimeout);

        this.loop = new ControlLoop(
            new PidController(config.gains(), PidController.Direction.DIRECT, config.limits()),
            () -> {
                SensorService.Reading reading = sensor.get("/reading", SensorService.Reading.class);
                return new ControlLoop.Reading(reading.value(), reading.ageMillis());
            },
            command -> actuator.post("/command", new ActuatorService.Command(command), ActuatorService.State.class),
            config.loop(),
            System::nanoTime);
        loop.setSetpoint(config.initialSetpoint());

        Http.Client sensorAdmin = new Http.Client(config.sensorUri(), Duration.ofSeconds(1));
        Http.Client actuatorAdmin = new Http.Client(config.actuatorUri(), Duration.ofSeconds(1));

        this.server = Http.start(config.port(), Map.of(
            "/", Http.resource("/web/index.html", "text/html; charset=utf-8"),
            "/app.js", Http.resource("/web/app.js", "text/javascript; charset=utf-8"),
            "/style.css", Http.resource("/web/style.css", "text/css; charset=utf-8"),
            "/events", events.handler(),
            "/api/settings", Http.methods(Map.of(
                "GET", exchange -> settings(),
                "POST", exchange -> update(Http.readBody(exchange, SettingsUpdate.class)))),
            "/api/history", Http.get(exchange -> history()),
            "/api/sensor/faults", Http.methods(proxy(sensorAdmin, "/faults")),
            "/api/actuator/faults", Http.methods(proxy(actuatorAdmin, "/faults")),
            "/api/plant", Http.methods(proxy(sensorAdmin, "/plant"))));

        long periodMillis = config.sampleTime().toMillis();
        scheduler.scheduleAtFixedRate(this::tick, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            changeDemoSetpointIfDue();
            ControlLoop.Sample sample = loop.tick();
            synchronized (history) {
                history.addLast(sample);
                if (history.size() > HISTORY_SIZE) {
                    history.removeFirst();
                }
            }
            events.publish(sample);
        } catch (RuntimeException ex) {
            // An exception would silently cancel the scheduled loop, so log it and keep going.
            System.getLogger(ControllerService.class.getName()).log(System.Logger.Level.ERROR, "Control tick failed", ex);
        }
    }

    private void changeDemoSetpointIfDue() {
        long now = System.nanoTime();
        if (demo && now - lastDemoChangeNanos >= DEMO_SETPOINT_PERIOD_NANOS) {
            loop.setSetpoint(ThreadLocalRandom.current().nextInt(20, 121));
            lastDemoChangeNanos = now;
        }
    }

    Settings settings() {
        PidController.Gains gains = loop.gains();

        return new Settings(loop.setpoint(), gains.kp(), gains.ki(), gains.kd(), loop.direction(), loop.manual(),
            loop.manualOutput(), demo, loop.limits().min(), loop.limits().max(), config.sampleTime().toMillis());
    }

    Settings update(SettingsUpdate update) {
        PidController.Gains current = loop.gains();
        PidController.Gains gains = new PidController.Gains(
            orElse(update.kp(), current.kp()),
            orElse(update.ki(), current.ki()),
            orElse(update.kd(), current.kd()));

        if (update.setpoint() != null) {
            loop.setSetpoint(update.setpoint());
        }
        loop.setGains(gains);
        if (update.direction() != null) {
            loop.setDirection(update.direction());
        }
        if (update.manual() != null || update.manualOutput() != null) {
            loop.setManual(orElse(update.manual(), loop.manual()), orElse(update.manualOutput(), loop.manualOutput()));
        }
        if (update.demo() != null) {
            demo = update.demo();
            lastDemoChangeNanos = System.nanoTime();
        }

        return settings();
    }

    List<ControlLoop.Sample> history() {
        synchronized (history) {
            return List.copyOf(history);
        }
    }

    private static Map<String, Http.Endpoint> proxy(Http.Client client, String path) {
        return Map.of(
            "GET", exchange -> callService(() -> client.get(path, JsonNode.class)),
            "POST", exchange -> {
                JsonNode body = Http.readBody(exchange, JsonNode.class);
                return callService(() -> client.post(path, body, JsonNode.class));
            });
    }

    private interface ServiceCall {
        JsonNode call() throws IOException;
    }

    private static JsonNode callService(ServiceCall call) {
        try {
            return call.call();
        } catch (Http.RemoteStatus ex) {
            // Pass the service's own rejection (e.g. invalid plant parameters) through to the browser.
            throw new Http.Status(ex.code() < 500 ? ex.code() : 502, ex.getMessage());
        } catch (IOException ex) {
            throw new Http.Status(502, "Service unreachable: " + ex.getMessage());
        }
    }

    private static <T> T orElse(T value, T fallback) {
        return value != null ? value : fallback;
    }

    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
        server.stop(0);
    }
}
