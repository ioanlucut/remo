package org.ilu.remo.actuator;

import com.sun.net.httpserver.HttpServer;
import org.ilu.remo.http.FaultInjector;
import org.ilu.remo.http.Http;

import java.time.Duration;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * The remote actuator, e.g. a valve whose opening is a percentage.
 *
 * <p>It has a watchdog: if no command arrives within {@code watchdog}, it moves to the safe position on its own. A
 * field device cannot rely on the controller being reachable in order to fail safely.
 *
 * <ul>
 *   <li>{@code POST /command} {@code {"value": 42.0}}: from the controller, behind fault injection</li>
 *   <li>{@code GET /state}: the position actually applied, read by the plant</li>
 *   <li>{@code GET|POST /faults}: the simulated network faults on the controller link</li>
 * </ul>
 */
public final class ActuatorService implements AutoCloseable {

    public record Config(int port, Duration watchdog, double safeValue, double minValue, double maxValue) {
        public static Config defaults(int port) {
            return new Config(port, Duration.ofSeconds(1), 0, 0, 100);
        }
    }

    public record Command(double value) {
    }

    public record State(double applied, boolean watchdogTripped) {
    }

    private final Config config;
    private final LongSupplier nanoClock;
    private final FaultInjector faults = new FaultInjector();
    private final HttpServer server;

    private double commanded;
    private long lastCommandNanos;
    private boolean everCommanded;

    public ActuatorService(Config config) {
        this(config, System::nanoTime);
    }

    ActuatorService(Config config, LongSupplier nanoClock) {
        this.config = config;
        this.nanoClock = nanoClock;
        this.server = Http.start(config.port(), Map.of(
            "/command", Http.methods(Map.of("POST", faults.wrap(exchange -> apply(Http.readBody(exchange, Command.class))))),
            "/state", Http.get(exchange -> state()),
            "/faults", Http.methods(faults.endpoints())));
    }

    synchronized State apply(Command command) {
        if (!Double.isFinite(command.value())) {
            throw new IllegalArgumentException("value must be finite, was " + command.value());
        }
        commanded = Math.max(config.minValue(), Math.min(config.maxValue(), command.value()));
        lastCommandNanos = nanoClock.getAsLong();
        everCommanded = true;

        return state();
    }

    synchronized State state() {
        boolean tripped = !everCommanded || nanoClock.getAsLong() - lastCommandNanos > config.watchdog().toNanos();

        return new State(tripped ? config.safeValue() : commanded, tripped);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
