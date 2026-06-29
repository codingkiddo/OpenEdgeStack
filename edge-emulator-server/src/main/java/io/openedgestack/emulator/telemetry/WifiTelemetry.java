package io.openedgestack.emulator.telemetry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record WifiTelemetry(
        @NotBlank String deviceId,
        @NotNull Integer rssi,
        @NotNull Integer snr,
        @NotNull Integer latencyMs,
        @NotNull Double packetLossPercent,
        @NotNull Double retryRatePercent,
        @NotNull Double rxMbps,
        @NotNull Double txMbps,
        Instant timestamp
) {
    public WifiTelemetry {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
