package org.ilu.remo.controller;

import org.ilu.remo.actuator.ActuatorService;
import org.ilu.remo.control.ControlLoop;
import org.ilu.remo.http.Http;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.function.LongSupplier;

/**
 * Time-bounded, ordered access to the remote actuator.
 *
 * <p>An unacknowledged command stays pending. On the next attempt the link first asks the actuator whether that
 * sequence was applied, so a lost response cannot make the controller guess. Expired commands are rejected by the
 * actuator even if their HTTP handlers finish after the caller's deadline.
 */
final class ActuatorLink implements ControlLoop.Actuator {

    private static final long SEQUENCES_PER_MILLISECOND = 1_000;

    private final Http.Client client;
    private final Duration timeout;
    private final LongSupplier epochMillisClock;

    private long lastSequence;
    private ActuatorService.Command pending;

    ActuatorLink(URI actuatorUri, Duration timeout) {
        this(new Http.Client(actuatorUri, timeout), timeout, System::currentTimeMillis);
    }

    ActuatorLink(Http.Client client, Duration timeout, LongSupplier epochMillisClock) {
        this.client = client;
        this.timeout = timeout;
        this.epochMillisClock = epochMillisClock;
    }

    @Override
    public double apply(double desiredOutput) throws IOException {
        if (pending != null) {
            ActuatorService.State state = client.get("/command", ActuatorService.State.class);
            if (state.lastSequence() >= pending.sequence()) {
                lastSequence = Math.max(lastSequence, state.lastSequence());
                pending = null;
                return state.applied();
            }
            if (epochMillisClock.getAsLong() < pending.expiresAtEpochMillis()) {
                return sendPending();
            }
            pending = null;
        }

        long now = epochMillisClock.getAsLong();
        long timeBasedSequence = Math.multiplyExact(now, SEQUENCES_PER_MILLISECOND);
        lastSequence = Math.max(lastSequence + 1, timeBasedSequence);
        pending = new ActuatorService.Command(desiredOutput, lastSequence, now + timeout.toMillis());

        return sendPending();
    }

    private double sendPending() throws IOException {
        ActuatorService.State state = client.post("/command", pending, ActuatorService.State.class);
        if (state.lastSequence() != pending.sequence()) {
            throw new IOException("Actuator acknowledged sequence " + state.lastSequence()
                + " instead of " + pending.sequence());
        }
        pending = null;

        return state.applied();
    }
}
