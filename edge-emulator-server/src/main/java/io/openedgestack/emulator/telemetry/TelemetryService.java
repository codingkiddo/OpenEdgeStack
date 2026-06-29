package io.openedgestack.emulator.telemetry;

import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.device.DeviceService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TelemetryService {

    private final StateStore stateStore;
    private final DeviceService deviceService;

    public TelemetryService(StateStore stateStore, DeviceService deviceService) {
        this.stateStore = stateStore;
        this.deviceService = deviceService;
    }

    public TelemetryIngestResponse ingestWifi(WifiTelemetry telemetry) {
        validateRanges(telemetry);
        deviceService.get(telemetry.deviceId());
        stateStore.addWifiTelemetry(telemetry.deviceId(), telemetry);
        return new TelemetryIngestResponse("ACCEPTED", telemetry.deviceId(), "WIFI", telemetry.timestamp());
    }

    public List<WifiTelemetry> listWifi(String deviceId) {
        deviceService.get(deviceId);
        return stateStore.wifiTelemetryForDevice(deviceId).stream()
                .sorted(Comparator.comparing(WifiTelemetry::timestamp))
                .toList();
    }

    public Optional<WifiTelemetry> latestWifi(String deviceId) {
        deviceService.get(deviceId);
        return stateStore.wifiTelemetryForDevice(deviceId).stream()
                .max(Comparator.comparing(WifiTelemetry::timestamp));
    }

    private void validateRanges(WifiTelemetry telemetry) {
        if (telemetry.packetLossPercent() < 0 || telemetry.packetLossPercent() > 100) {
            throw new IllegalArgumentException("packetLossPercent must be between 0 and 100");
        }
        if (telemetry.retryRatePercent() < 0 || telemetry.retryRatePercent() > 100) {
            throw new IllegalArgumentException("retryRatePercent must be between 0 and 100");
        }
        if (telemetry.latencyMs() < 0) {
            throw new IllegalArgumentException("latencyMs must be non-negative");
        }
    }
}
