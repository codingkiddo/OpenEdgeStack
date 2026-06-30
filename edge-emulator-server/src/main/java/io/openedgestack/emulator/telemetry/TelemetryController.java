package io.openedgestack.emulator.telemetry;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for telemetry ingestion and retrieval.
 *
 * <p>Currently supports the Wi-Fi telemetry channel. Additional channels
 * (e.g. LAN, WAN) can be added by extending this controller and
 * {@link TelemetryService}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /telemetry/wifi}               — ingest a Wi-Fi sample for a device</li>
 *   <li>{@code GET  /telemetry/wifi?deviceId=...}  — retrieve all samples for a device</li>
 * </ul>
 */
@RestController
class TelemetryController {

    private final TelemetryService telemetryService;

    TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    /**
     * Ingests a single Wi-Fi telemetry sample.
     * Returns 200 OK with an {@link TelemetryIngestResponse} on success,
     * 400 Bad Request if fields are missing or out of range,
     * or 404 Not Found if the device does not exist.
     */
    @PostMapping("/telemetry/wifi")
    TelemetryIngestResponse ingestWifi(@Valid @RequestBody WifiTelemetry telemetry) {
        return telemetryService.ingestWifi(telemetry);
    }

    /**
     * Returns all Wi-Fi telemetry samples for the specified device, sorted by timestamp ascending.
     * Returns 404 Not Found if the device does not exist.
     */
    @GetMapping("/telemetry/wifi")
    List<WifiTelemetry> listWifi(@RequestParam String deviceId) {
        return telemetryService.listWifi(deviceId);
    }
}
