package org.ilu.remo.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpTest {

    @Test
    void serverBindsToLoopbackByDefault() {
        HttpServer server = Http.start(0, Map.of("/health", Http.get(exchange -> Map.of("status", "ok"))));
        try {
            assertThat(server.getAddress().getAddress().isLoopbackAddress()).isTrue();
        } finally {
            server.stop(0);
        }
    }
}
