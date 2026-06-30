package io.openedgestack.emulator.telemetry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * A single Wi-Fi telemetry sample reported by a device.
 *
 * <p>All numeric fields are validated at ingestion time — range constraints
 * (percentages 0–100, non-negative latency) are enforced in
 * {@link TelemetryService#ingestWifi(WifiTelemetry)}.
 *
 * @param deviceId           the reporting device's ID (required)
 * @param rssi               received signal strength indicator in dBm; must be negative (typically –30 to –90)
 * @param snr                signal-to-noise ratio in dB
 * @param latencyMs          round-trip latency in milliseconds; must be ≥ 0
 * @param packetLossPercent  percentage of lost packets; must be in [0, 100]
 * @param retryRatePercent   percentage of retried transmissions; must be in [0, 100]
 * @param rxMbps             downstream throughput in Mbps
 * @param txMbps             upstream throughput in Mbps
 * @param timestamp          UTC instant of measurement; auto-set to now if omitted
 */
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
    /** Compact constructor — supplies the current time when the caller omits a timestamp. */
    public WifiTelemetry {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
