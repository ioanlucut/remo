package org.ilu.remo.controller;

import com.sun.net.httpserver.HttpServer;
import org.ilu.remo.actuator.ActuatorService;
import org.ilu.remo.http.Http;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActuatorLinkTest {

    @Test
    void reconcilesAnAppliedCommandWhenItsResponseWasLost() throws Exception {
        AtomicInteger posts = new AtomicInteger();
        AtomicReference<ActuatorService.State> state = new AtomicReference<>(
            new ActuatorService.State(0, true, -1));
        HttpServer server = Http.start(0, Map.of("/command", exchange -> {
            try (exchange) {
                if (exchange.getRequestMethod().equals("POST")) {
                    ActuatorService.Command command = Http.readBody(exchange, ActuatorService.Command.class);
                    state.set(new ActuatorService.State(command.value(), false, command.sequence()));
                    posts.incrementAndGet();
                    return; // Close without response headers: the command was applied but its acknowledgement was lost.
                }
                Http.sendJson(exchange, 200, state.get());
            }
        }));

        try {
            URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/");
            ActuatorLink link = new ActuatorLink(uri, Duration.ofMillis(100));

            assertThatThrownBy(() -> link.apply(80)).isInstanceOf(IOException.class);

            assertThat(link.apply(0)).isEqualTo(80);
            assertThat(posts).hasValue(1);
        } finally {
            server.stop(0);
        }
    }
}
