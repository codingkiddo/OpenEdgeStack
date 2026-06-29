package io.openedgestack.emulator.telemetry;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class TelemetryController {

    private final TelemetryService telemetryService;

    TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping("/telemetry/wifi")
    TelemetryIngestResponse ingestWifi(@Valid @RequestBody WifiTelemetry telemetry) {
        return telemetryService.ingestWifi(telemetry);
    }

    @GetMapping("/telemetry/wifi")
    List<WifiTelemetry> listWifi(@RequestParam String deviceId) {
        return telemetryService.listWifi(deviceId);
    }
}
