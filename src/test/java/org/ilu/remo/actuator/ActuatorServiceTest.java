package org.ilu.remo.actuator;

import com.fasterxml.jackson.databind.JsonNode;
import org.ilu.remo.http.FaultInjector;
import org.ilu.remo.http.Http;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActuatorServiceTest {

    @Test
    @Timeout(3)
    void commandThatMissesItsDeadlineCannotMoveTheActuatorLater() throws Exception {
        try (ActuatorService actuator = new ActuatorService(
            new ActuatorService.Config(0, Duration.ofSeconds(5), 0, 0, 100))) {
            URI uri = URI.create("http://localhost:" + actuator.port() + "/");
            Http.Client admin = new Http.Client(uri, Duration.ofSeconds(1));
            Http.Client controller = new Http.Client(uri, Duration.ofMillis(20));
            admin.post("/faults", new FaultInjector.Faults(false, 100, 0), JsonNode.class);

            long expiresAt = System.currentTimeMillis() + 20;
            assertThatThrownBy(() -> controller.post("/command", new ActuatorService.Command(80, 1, expiresAt),
                ActuatorService.State.class)).isInstanceOf(java.io.IOException.class);

            Thread.sleep(200);

            ActuatorService.State state = admin.get("/state", ActuatorService.State.class);
            assertThat(state.applied()).isZero();
            assertThat(state.watchdogTripped()).isTrue();
        }
    }

    @Test
    void olderCommandCannotOverwriteANewerCommand() throws Exception {
        try (ActuatorService actuator = new ActuatorService(
            new ActuatorService.Config(0, Duration.ofSeconds(5), 0, 0, 100))) {
            Http.Client client = new Http.Client(
                URI.create("http://localhost:" + actuator.port() + "/"), Duration.ofSeconds(1));
            long expiresAt = System.currentTimeMillis() + 1_000;

            ActuatorService.State accepted = client.post("/command",
                new ActuatorService.Command(80, 2, expiresAt), ActuatorService.State.class);
            assertThat(accepted.lastSequence()).isEqualTo(2);

            assertThatThrownBy(() -> client.post("/command",
                new ActuatorService.Command(20, 1, expiresAt), ActuatorService.State.class))
                .isInstanceOf(Http.RemoteStatus.class)
                .hasMessageContaining("older");

            assertThat(client.get("/state", ActuatorService.State.class).applied()).isEqualTo(80);
        }
    }
}
