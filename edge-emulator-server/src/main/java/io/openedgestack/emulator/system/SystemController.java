package io.openedgestack.emulator.system;

import io.openedgestack.emulator.common.StateStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
class SystemController {

    private final StateStore stateStore;

    SystemController(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @GetMapping("/_oes/health")
    Map<String, Object> health() {
        return Map.of(
                "service", "open-edge-stack",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }

    @PostMapping("/_oes/reset")
    Map<String, Object> reset() {
        stateStore.reset();
        return Map.of(
                "status", "RESET",
                "timestamp", Instant.now().toString()
        );
    }
}
