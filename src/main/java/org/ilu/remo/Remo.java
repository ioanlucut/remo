package org.ilu.remo;

import org.ilu.remo.actuator.ActuatorService;
import org.ilu.remo.controller.ControllerService;
import org.ilu.remo.sensor.SensorService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Starts the Remo services.
 *
 * <pre>
 *   java -jar remo.jar              all three services in one JVM (still talking over HTTP)
 *   java -jar remo.jar controller   one service per process, e.g. on different machines
 *   java -jar remo.jar sensor
 *   java -jar remo.jar actuator
 * </pre>
 *
 * <p>Ports and addresses come from environment variables: {@code REMO_BIND_HOST} (127.0.0.1),
 * {@code REMO_CONTROLLER_PORT} (8080), {@code REMO_SENSOR_PORT} (8081), {@code REMO_ACTUATOR_PORT} (8082),
 * {@code REMO_SENSOR_URL} and {@code REMO_ACTUATOR_URL} (localhost on those ports).
 */
public final class Remo {

    private Remo() {
    }

    public static void main(String[] args) throws InterruptedException {
        String role = args.length > 0 ? args[0] : "all";
        String bindHost = env("REMO_BIND_HOST", "127.0.0.1");
        int controllerPort = intEnv("REMO_CONTROLLER_PORT", 8080);
        int sensorPort = intEnv("REMO_SENSOR_PORT", 8081);
        int actuatorPort = intEnv("REMO_ACTUATOR_PORT", 8082);
        URI sensorUri = URI.create(env("REMO_SENSOR_URL", "http://localhost:" + sensorPort + "/"));
        URI actuatorUri = URI.create(env("REMO_ACTUATOR_URL", "http://localhost:" + actuatorPort + "/"));

        List<AutoCloseable> services = new ArrayList<>();
        if (role.equals("all") || role.equals("actuator")) {
            services.add(new ActuatorService(ActuatorService.Config.defaults(bindHost, actuatorPort)));
            System.out.println("Actuator   http://localhost:" + actuatorPort + "/state");
        }
        if (role.equals("all") || role.equals("sensor")) {
            services.add(new SensorService(SensorService.Config.defaults(bindHost, sensorPort, actuatorUri)));
            System.out.println("Sensor     http://localhost:" + sensorPort + "/reading");
        }
        if (role.equals("all") || role.equals("controller")) {
            services.add(new ControllerService(
                ControllerService.Config.defaults(bindHost, controllerPort, sensorUri, actuatorUri)));
            System.out.println("Controller http://localhost:" + controllerPort + "/");
        }
        if (services.isEmpty()) {
            System.err.println("Unknown role '" + role + "'. Use one of: all, controller, sensor, actuator.");
            System.exit(2);
        }

        CountDownLatch stopped = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            services.reversed().forEach(Remo::closeQuietly);
            stopped.countDown();
        }));
        stopped.await();
    }

    private static void closeQuietly(AutoCloseable service) {
        try {
            service.close();
        } catch (Exception ex) {
            System.err.println("Failed to stop " + service + ": " + ex);
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int intEnv(String name, int fallback) {
        return Integer.parseInt(env(name, String.valueOf(fallback)));
    }
}
