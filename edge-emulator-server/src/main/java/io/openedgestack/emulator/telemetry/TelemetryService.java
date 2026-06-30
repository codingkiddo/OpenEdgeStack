package io.openedgestack.emulator.telemetry;

import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.device.DeviceService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for telemetry ingestion and retrieval.
 *
 * <p>Validates that the target device exists before storing any sample, so telemetry
 * can never be orphaned from a device record. Range validation (percentages, latency)
 * is enforced here rather than via Bean Validation annotations so that the error
 * message can be precise about which field is out of range.
 */
@Service
public class TelemetryService {

    private final StateStore stateStore;
    private final DeviceService deviceService;

    public TelemetryService(StateStore stateStore, DeviceService deviceService) {
        this.stateStore = stateStore;
        this.deviceService = deviceService;
    }

    /**
     * Validates and stores a Wi-Fi telemetry sample.
     *
     * @param telemetry the sample to ingest; ranges are validated before storage
     * @return an acknowledgement confirming the sample was accepted
     * @throws IllegalArgumentException if any numeric field is out of its valid range
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     */
    public TelemetryIngestResponse ingestWifi(WifiTelemetry telemetry) {
        // Range checks run first so we return 400 before touching the device registry.
        validateRanges(telemetry);
        // Confirm the device exists — rejects samples for unknown devices with 404.
        deviceService.get(telemetry.deviceId());
        stateStore.addWifiTelemetry(telemetry.deviceId(), telemetry);
        return new TelemetryIngestResponse("ACCEPTED", telemetry.deviceId(), "WIFI", telemetry.timestamp());
    }

    /**
     * Returns all Wi-Fi samples for a device sorted by timestamp ascending.
     *
     * @param deviceId the device to query
     * @return chronologically ordered list of samples; empty if none have been ingested
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     */
    public List<WifiTelemetry> listWifi(String deviceId) {
        deviceService.get(deviceId);
        return stateStore.wifiTelemetryForDevice(deviceId).stream()
                .sorted(Comparator.comparing(WifiTelemetry::timestamp))
                .toList();
    }

    /**
     * Returns the most recent Wi-Fi sample for a device.
     *
     * <p>Used by {@link io.openedgestack.emulator.scoring.ScoringService} to score
     * the device's current network quality. Returns an empty {@link Optional} if no
     * telemetry has been ingested yet.
     *
     * @param deviceId the device to query
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     */
    public Optional<WifiTelemetry> latestWifi(String deviceId) {
        deviceService.get(deviceId);
        return stateStore.wifiTelemetryForDevice(deviceId).stream()
                .max(Comparator.comparing(WifiTelemetry::timestamp));
    }

    /**
     * Returns the most recent Wi-Fi sample for a device, or throws if none exists yet.
     *
     * <p>Exposed via {@code GET /devices/{deviceId}/telemetry/wifi/latest}.
     * The scoring path uses the same underlying {@link #latestWifi(String)} but tolerates
     * an empty result; this method surfaces a clear 404 instead.
     *
     * @param deviceId the device to query
     * @return the latest sample ordered by timestamp
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     *         or no telemetry has been ingested yet
     */
    public WifiTelemetry getLatestWifi(String deviceId) {
        return latestWifi(deviceId).orElseThrow(
                () -> new io.openedgestack.emulator.common.NotFoundException(
                        "No Wi-Fi telemetry found for device " + deviceId));
    }

    /**
     * Guards against physically impossible or nonsensical telemetry values.
     * Bean Validation handles missing fields; this method handles out-of-range values.
     */
    private void validateRanges(WifiTelemetry telemetry) {
        if (telemetry.rssi() >= 0) {
            throw new IllegalArgumentException("rssi must be negative (received " + telemetry.rssi() + ")");
        }
        if (telemetry.latencyMs() < 0) {
            throw new IllegalArgumentException("latencyMs must be non-negative");
        }
        if (telemetry.packetLossPercent() < 0 || telemetry.packetLossPercent() > 100) {
            throw new IllegalArgumentException("packetLossPercent must be between 0 and 100");
        }
        if (telemetry.retryRatePercent() < 0 || telemetry.retryRatePercent() > 100) {
            throw new IllegalArgumentException("retryRatePercent must be between 0 and 100");
        }
    }
}
