package org.ilu.remo.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.ilu.remo.http.Http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Server-sent events: every published object goes to every connected browser as one JSON {@code data:} line. */
final class EventStream {

    private record Subscriber(HttpExchange exchange, OutputStream out) {
    }

    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    /** Keeps the response open; it is closed when the browser disconnects or the server stops. */
    HttpHandler handler() {
        return exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, 0);
            subscribers.add(new Subscriber(exchange, exchange.getResponseBody()));
        };
    }

    void publish(Object event) {
        if (subscribers.isEmpty()) {
            return;
        }
        byte[] frame;
        try {
            frame = ("data: " + Http.JSON.writeValueAsString(event) + "\n\n").getBytes(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Event is not serialisable", ex);
        }
        for (Subscriber subscriber : subscribers) {
            try {
                subscriber.out().write(frame);
                subscriber.out().flush();
            } catch (IOException ex) {
                subscribers.remove(subscriber);
                subscriber.exchange().close();
            }
        }
    }

    int subscriberCount() {
        return subscribers.size();
    }
}
