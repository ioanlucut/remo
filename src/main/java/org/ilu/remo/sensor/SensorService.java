package org.ilu.remo.sensor;

import com.sun.net.httpserver.HttpServer;
import org.ilu.remo.actuator.ActuatorService;
import org.ilu.remo.http.FaultInjector;
import org.ilu.remo.http.Http;
import org.ilu.remo.plant.FirstOrderPlant;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The remote sensor, measuring a simulated process.
 *
 * <p>The process runs on its own clock and is driven by the position the actuator actually applied, which it reads
 * from the actuator. If that read fails, the last known position stays in effect, as a real valve would stay put.
 *
 * <ul>
 *   <li>{@code GET /reading}: the process value and its age, behind fault injection</li>
 *   <li>{@code GET|POST /plant}: the process parameters (gain, time constant, dead time)</li>
 *   <li>{@code GET|POST /faults}: the simulated network faults on the controller link</li>
 * </ul>
 */
public final class SensorService implements AutoCloseable {

    public record Config(String bindHost, int port, URI actuatorUri, Duration plantStep,
                         FirstOrderPlant.Parameters plant) {
        public Config(int port, URI actuatorUri, Duration plantStep, FirstOrderPlant.Parameters plant) {
            this("127.0.0.1", port, actuatorUri, plantStep, plant);
        }

        public static Config defaults(int port, URI actuatorUri) {
            return defaults("127.0.0.1", port, actuatorUri);
        }

        public static Config defaults(String bindHost, int port, URI actuatorUri) {
            return new Config(bindHost, port, actuatorUri, Duration.ofMillis(20),
                new FirstOrderPlant.Parameters(1.5, 8, 1));
        }
    }

    public record Reading(double value, long ageMillis) {
    }

    private static final System.Logger LOGGER = System.getLogger(SensorService.class.getName());

    private final FirstOrderPlant plant;
    private final Http.Client actuator;
    private final FaultInjector faults = new FaultInjector();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, Thread.ofPlatform()
        .name("sensor-", 0)
        .daemon()
        .factory());
    private final HttpServer server;

    private volatile double valvePosition;
    private volatile boolean actuatorReachable = true;
    private long lastStepNanos = System.nanoTime();

    public SensorService(Config config) {
        this.plant = new FirstOrderPlant(config.plant(), config.plantStep().toNanos() / 1e9, 0);
        this.actuator = new Http.Client(config.actuatorUri(), Duration.ofMillis(200));

        long stepMillis = config.plantStep().toMillis();
        scheduler.scheduleAtFixedRate(this::stepPlant, 0, stepMillis, TimeUnit.MILLISECONDS);
        scheduler.scheduleWithFixedDelay(this::readValvePosition, 0, Math.max(stepMillis, 50), TimeUnit.MILLISECONDS);

        this.server = Http.start(config.bindHost(), config.port(), Map.of(
            "/reading", Http.get(faults.wrap(exchange -> reading())),
            "/plant", Http.methods(Map.of(
                "GET", exchange -> parameters(),
                "POST", exchange -> setParameters(Http.readBody(exchange, FirstOrderPlant.Parameters.class)))),
            "/faults", Http.methods(faults.endpoints())));
    }

    private synchronized void stepPlant() {
        plant.step(valvePosition);
        lastStepNanos = System.nanoTime();
    }

    private void readValvePosition() {
        try {
            valvePosition = actuator.get("/state", ActuatorService.State.class).applied();
            if (!actuatorReachable) {
                LOGGER.log(System.Logger.Level.INFO, "Actuator reachable again");
            }
            actuatorReachable = true;
        } catch (IOException | RuntimeException ex) {
            if (actuatorReachable) {
                LOGGER.log(System.Logger.Level.WARNING, "Cannot read actuator position, keeping the last one: " + ex);
            }
            actuatorReachable = false;
        }
    }

    synchronized Reading reading() {
        long ageMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastStepNanos);

        return new Reading(plant.value(), ageMillis);
    }

    synchronized FirstOrderPlant.Parameters parameters() {
        return plant.parameters();
    }

    synchronized FirstOrderPlant.Parameters setParameters(FirstOrderPlant.Parameters parameters) {
        plant.setParameters(parameters);

        return plant.parameters();
    }

    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
        scheduler.shutdownNow();
    }
}
