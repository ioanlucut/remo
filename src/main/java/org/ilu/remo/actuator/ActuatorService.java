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
 *   <li>{@code POST /command}: an ordered, expiring command from the controller, behind fault injection</li>
 *   <li>{@code GET /command}: the last command acknowledgement, behind the same fault injection</li>
 *   <li>{@code GET /state}: the position actually applied, read by the plant</li>
 *   <li>{@code GET|POST /faults}: the simulated network faults on the controller link</li>
 * </ul>
 */
public final class ActuatorService implements AutoCloseable {

    public record Config(String bindHost, int port, Duration watchdog, double safeValue, double minValue,
                         double maxValue) {
        public Config(int port, Duration watchdog, double safeValue, double minValue, double maxValue) {
            this("127.0.0.1", port, watchdog, safeValue, minValue, maxValue);
        }

        public static Config defaults(int port) {
            return defaults("127.0.0.1", port);
        }

        public static Config defaults(String bindHost, int port) {
            return new Config(bindHost, port, Duration.ofSeconds(1), 0, 0, 100);
        }
    }

    /** A time-bounded, ordered command. The sequence must increase across controller restarts. */
    public record Command(double value, long sequence, long expiresAtEpochMillis) {
    }

    /** {@code lastSequence} acknowledges the newest command accepted by this actuator. */
    public record State(double applied, boolean watchdogTripped, long lastSequence) {
    }

    private final Config config;
    private final LongSupplier nanoClock;
    private final LongSupplier epochMillisClock;
    private final FaultInjector faults = new FaultInjector();
    private final HttpServer server;

    private double commanded;
    private long lastCommandNanos;
    private long lastSequence = -1;
    private boolean everCommanded;

    public ActuatorService(Config config) {
        this(config, System::nanoTime, System::currentTimeMillis);
    }

    ActuatorService(Config config, LongSupplier nanoClock, LongSupplier epochMillisClock) {
        this.config = config;
        this.nanoClock = nanoClock;
        this.epochMillisClock = epochMillisClock;
        this.server = Http.start(config.bindHost(), config.port(), Map.of(
            "/command", Http.methods(Map.of(
                "GET", faults.wrap(exchange -> state()),
                "POST", faults.wrap(exchange -> apply(Http.readBody(exchange, Command.class))))),
            "/state", Http.get(exchange -> state()),
            "/faults", Http.methods(faults.endpoints())));
    }

    synchronized State apply(Command command) {
        if (!Double.isFinite(command.value())) {
            throw new IllegalArgumentException("value must be finite, was " + command.value());
        }
        if (command.sequence() < 0) {
            throw new IllegalArgumentException("sequence must be non-negative, was " + command.sequence());
        }

        double requested = Math.max(config.minValue(), Math.min(config.maxValue(), command.value()));
        if (command.sequence() == lastSequence) {
            if (Double.compare(requested, commanded) != 0) {
                throw new Http.Status(409, "Sequence already belongs to a different command");
            }
            return state();
        }
        if (command.sequence() < lastSequence) {
            throw new Http.Status(409, "Command is older than the last accepted command");
        }
        if (epochMillisClock.getAsLong() >= command.expiresAtEpochMillis()) {
            throw new Http.Status(409, "Command expired before it reached the actuator");
        }

        commanded = requested;
        lastSequence = command.sequence();
        lastCommandNanos = nanoClock.getAsLong();
        everCommanded = true;

        return state();
    }

    synchronized State state() {
        boolean tripped = !everCommanded || nanoClock.getAsLong() - lastCommandNanos > config.watchdog().toNanos();

        return new State(tripped ? config.safeValue() : commanded, tripped, lastSequence);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
