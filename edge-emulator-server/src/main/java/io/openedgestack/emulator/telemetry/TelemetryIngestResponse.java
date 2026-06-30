package io.openedgestack.emulator.telemetry;

import java.time.Instant;

/**
 * Acknowledgement returned after a telemetry sample is successfully ingested.
 *
 * @param status    result of the ingestion, always {@code "ACCEPTED"} on success
 * @param deviceId  the device the sample was attributed to
 * @param type      telemetry channel that was written, e.g. {@code "WIFI"}
 * @param timestamp the measurement timestamp echoed back from the ingested sample
 */
public record TelemetryIngestResponse(
        String status,
        String deviceId,
        String type,
        Instant timestamp
) {
}
