package io.openedgestack.emulator.scoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for device quality and security scoring.
 *
 * <p>Scores are computed on-demand from the device's existing telemetry and DNS
 * decision history — no separate scoring pipeline is needed.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /devices/{deviceId}/qoe-score}  — Wi-Fi quality-of-experience score (0–100)</li>
 *   <li>{@code GET /devices/{deviceId}/risk-score} — security risk score based on DNS blocks (0–100)</li>
 * </ul>
 */
@RestController
class ScoringController {

    private final ScoringService scoringService;

    ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    /**
     * Returns the QoE score for the device based on its latest Wi-Fi telemetry sample.
     * Returns HTTP 404 if the device does not exist.
     */
    @GetMapping("/devices/{deviceId}/qoe-score")
    QoeScore qoeScore(@PathVariable String deviceId) {
        return scoringService.qoeScore(deviceId);
    }

    /**
     * Returns the risk score for the device based on its DNS decision history.
     * Returns HTTP 404 if the device does not exist.
     */
    @GetMapping("/devices/{deviceId}/risk-score")
    RiskScore riskScore(@PathVariable String deviceId) {
        return scoringService.riskScore(deviceId);
    }
}
