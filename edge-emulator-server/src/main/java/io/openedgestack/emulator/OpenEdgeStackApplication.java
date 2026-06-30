package io.openedgestack.emulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Open Edge Stack emulator server.
 *
 * <p>This Spring Boot application emulates an edge network management platform,
 * providing in-memory APIs for households, devices, telemetry, DNS policy,
 * firmware management, and quality/risk scoring. It is designed for local
 * development and integration testing — no database or message broker required.
 */
@SpringBootApplication
public class OpenEdgeStackApplication {

    /**
     * Bootstraps the Spring application context and starts the embedded web server.
     *
     * @param args command-line arguments passed through to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(OpenEdgeStackApplication.class, args);
    }
}
