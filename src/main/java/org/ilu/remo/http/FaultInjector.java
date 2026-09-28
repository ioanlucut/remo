package org.ilu.remo.http;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates an unreliable network link in front of an endpoint: extra latency, dropped requests, or the link down.
 * Only the endpoints the controller talks to are wrapped; the physical link between actuator and plant is not.
 */
public final class FaultInjector {

    /**
     * @param down          every request fails with 503
     * @param latencyMillis delay added before answering
     * @param dropRate      share of requests, from 0 to 1, that fail with 503
     */
    public record Faults(boolean down, long latencyMillis, double dropRate) {
        public static final Faults NONE = new Faults(false, 0, 0);

        public Faults {
            if (latencyMillis < 0 || latencyMillis > 10_000) {
                throw new IllegalArgumentException("latencyMillis must be between 0 and 10000, was " + latencyMillis);
            }
            if (!(dropRate >= 0 && dropRate <= 1)) {
                throw new IllegalArgumentException("dropRate must be between 0 and 1, was " + dropRate);
            }
        }
    }

    private volatile Faults faults = Faults.NONE;

    public Http.Endpoint wrap(Http.Endpoint endpoint) {
        return exchange -> {
            Faults current = faults;
            if (current.latencyMillis() > 0) {
                sleep(current.latencyMillis());
            }
            if (current.down() || ThreadLocalRandom.current().nextDouble() < current.dropRate()) {
                throw new Http.Status(503, "Link unavailable (injected fault)");
            }

            return endpoint.handle(exchange);
        };
    }

    /** {@code GET} shows the active faults, {@code POST} replaces them. */
    public Map<String, Http.Endpoint> endpoints() {
        return Map.of(
            "GET", exchange -> faults,
            "POST", exchange -> faults = Http.readBody(exchange, Faults.class));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
