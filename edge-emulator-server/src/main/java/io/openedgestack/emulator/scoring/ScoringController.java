package io.openedgestack.emulator.scoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ScoringController {

    private final ScoringService scoringService;

    ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @GetMapping("/devices/{deviceId}/qoe-score")
    QoeScore qoeScore(@PathVariable String deviceId) {
        return scoringService.qoeScore(deviceId);
    }

    @GetMapping("/devices/{deviceId}/risk-score")
    RiskScore riskScore(@PathVariable String deviceId) {
        return scoringService.riskScore(deviceId);
    }
}
