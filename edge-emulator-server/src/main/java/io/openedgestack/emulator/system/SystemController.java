package io.openedgestack.emulator.system;

import io.openedgestack.emulator.common.StateStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Internal system endpoints used for health checks and test lifecycle management.
 *
 * <p>All paths are prefixed with {@code /_oes/} to separate them from the
 * domain API endpoints and to signal that they are emulator-internal.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET  /_oes/health} — liveness probe; always returns {@code UP}</li>
 *   <li>{@code POST /_oes/reset}  — wipes all in-memory state for test isolation</li>
 * </ul>
 */
@RestController
class SystemController {

    private final StateStore stateStore;

    SystemController(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    /**
     * Liveness probe. Returns service name, status, and current UTC timestamp.
     * Used by Docker / orchestrators to confirm the server is up.
     */
    @GetMapping("/_oes/health")
    Map<String, Object> health() {
        return Map.of(
                "service", "open-edge-stack",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }

    /**
     * Clears all in-memory state and returns a confirmation with a UTC timestamp.
     *
     * <p>Called by {@code ModuleTestSupport#resetState()} in a {@code @BeforeEach} hook
     * so every test starts with a clean slate. Must not be called in production contexts.
     */
    @PostMapping("/_oes/reset")
    Map<String, Object> reset() {
        stateStore.reset();
        return Map.of(
                "status", "RESET",
                "timestamp", Instant.now().toString()
        );
    }
}
