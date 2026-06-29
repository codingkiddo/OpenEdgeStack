package io.openedgestack.emulator.telemetry;

import java.time.Instant;

public record TelemetryIngestResponse(
        String status,
        String deviceId,
        String type,
        Instant timestamp
) {
}
