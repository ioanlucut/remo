package org.ilu.remo.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;

/** Minimal JSON-over-HTTP plumbing on top of the JDK's built-in server and client. */
public final class Http {

    public static final ObjectMapper JSON = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    private static final System.Logger LOGGER = System.getLogger(Http.class.getName());

    private Http() {
    }

    /** Handles one request and returns the body to send back as JSON with status 200. */
    @FunctionalInterface
    public interface Endpoint {
        Object handle(HttpExchange exchange) throws IOException;
    }

    /** Thrown by an endpoint to answer with a status other than 200. */
    public static final class Status extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final int code;

        public Status(int code, String message) {
            super(message);
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    /** Starts a server on {@code port} (0 picks a free port) that runs every request on a virtual thread. */
    public static HttpServer start(int port, Map<String, HttpHandler> routes) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            routes.forEach(server::createContext);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();

            return server;
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not start HTTP server on port " + port, ex);
        }
    }

    /** Routes by HTTP method, answers 405 for anything else and turns failures into JSON errors. */
    public static HttpHandler methods(Map<String, Endpoint> endpointsByMethod) {
        return exchange -> {
            // The error responses must be sent before the exchange closes, so the catch blocks sit inside the try.
            try (exchange) {
                try {
                    Endpoint endpoint = endpointsByMethod.get(exchange.getRequestMethod());
                    if (endpoint == null) {
                        exchange.getResponseHeaders().add("Allow", String.join(", ", endpointsByMethod.keySet()));
                        throw new Status(405, "Method not allowed");
                    }
                    sendJson(exchange, 200, endpoint.handle(exchange));
                } catch (Status ex) {
                    sendJson(exchange, ex.code(), Map.of("error", ex.getMessage()));
                } catch (IllegalArgumentException | IOException ex) {
                    sendJson(exchange, 400, Map.of("error", rootMessage(ex)));
                } catch (RuntimeException ex) {
                    LOGGER.log(System.Logger.Level.ERROR, "Request failed: " + exchange.getRequestURI(), ex);
                    sendJson(exchange, 500, Map.of("error", "Internal error"));
                }
            }
        };
    }

    /** Jackson wraps a record's validation error; the innermost message is the one worth showing. */
    private static String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }

    public static HttpHandler get(Endpoint endpoint) {
        return methods(Map.of("GET", endpoint));
    }

    public static <T> T readBody(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            T value = JSON.readValue(body, type);
            if (value == null) {
                throw new IllegalArgumentException("Request body is required");
            }

            return value;
        }
    }

    public static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JSON.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /** Serves a file from the classpath at exactly the context path (not below it), or 404. */
    public static HttpHandler resource(String classpathPath, String contentType) {
        return exchange -> {
            try (exchange; InputStream in = Http.class.getResourceAsStream(classpathPath)) {
                boolean exactPath = exchange.getRequestURI().getPath().equals(exchange.getHttpContext().getPath());
                if (in == null || !exactPath) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                byte[] bytes = in.readAllBytes();
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        };
    }

    /** A JSON client for one remote service, with a hard timeout on every call. */
    public static final class Client {
        private final HttpClient client;
        private final URI baseUri;
        private final Duration timeout;

        public Client(URI baseUri, Duration timeout) {
            this.client = HttpClient.newBuilder().connectTimeout(timeout).build();
            this.baseUri = baseUri;
            this.timeout = timeout;
        }

        public <T> T get(String path, Class<T> type) throws IOException {
            return send(request(path).GET().build(), type);
        }

        public <T> T post(String path, Object body, Class<T> type) throws IOException {
            HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofByteArray(JSON.writeValueAsBytes(body));

            return send(request(path).header("Content-Type", "application/json").POST(publisher).build(), type);
        }

        private HttpRequest.Builder request(String path) {
            return HttpRequest.newBuilder(baseUri.resolve(path)).timeout(timeout);
        }

        private <T> T send(HttpRequest request, Class<T> type) throws IOException {
            try {
                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() != 200) {
                    throw new RemoteStatus(response.statusCode(), errorMessage(response.body()));
                }

                return JSON.readValue(response.body(), type);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while calling " + request.uri(), ex);
            }
        }

        private static String errorMessage(byte[] body) {
            try {
                return JSON.readTree(body).path("error").asText("Unknown error");
            } catch (IOException ex) {
                return new String(body, StandardCharsets.UTF_8);
            }
        }
    }

    /** A remote service answered with a status other than 200. */
    public static final class RemoteStatus extends IOException {
        private static final long serialVersionUID = 1L;

        private final int code;

        public RemoteStatus(int code, String message) {
            super(message);
            this.code = code;
        }

        public int code() {
            return code;
        }
    }
}
