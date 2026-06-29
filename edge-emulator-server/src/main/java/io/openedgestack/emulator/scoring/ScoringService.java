package io.openedgestack.emulator.scoring;

import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.device.DeviceService;
import io.openedgestack.emulator.dns.DnsAction;
import io.openedgestack.emulator.dns.DnsDecision;
import io.openedgestack.emulator.telemetry.TelemetryService;
import io.openedgestack.emulator.telemetry.WifiTelemetry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoringService {

    private final TelemetryService telemetryService;
    private final StateStore stateStore;
    private final DeviceService deviceService;

    public ScoringService(TelemetryService telemetryService, StateStore stateStore, DeviceService deviceService) {
        this.telemetryService = telemetryService;
        this.stateStore = stateStore;
        this.deviceService = deviceService;
    }

    public QoeScore qoeScore(String deviceId) {
        WifiTelemetry telemetry = telemetryService.latestWifi(deviceId).orElse(null);
        if (telemetry == null) {
            return new QoeScore(deviceId, 50, ScoreGrade.FAIR, List.of("No Wi-Fi telemetry available; returning neutral score"));
        }

        int score = 100;
        List<String> reasons = new ArrayList<>();

        if (telemetry.rssi() < -75) {
            score -= 25;
            reasons.add("Weak RSSI");
        } else if (telemetry.rssi() < -65) {
            score -= 10;
            reasons.add("Moderate RSSI");
        } else {
            reasons.add("RSSI is acceptable");
        }

        if (telemetry.latencyMs() > 150) {
            score -= 25;
            reasons.add("High latency");
        } else if (telemetry.latencyMs() > 80) {
            score -= 10;
            reasons.add("Moderate latency");
        } else {
            reasons.add("Latency is acceptable");
        }

        if (telemetry.packetLossPercent() > 5) {
            score -= 20;
            reasons.add("High packet loss");
        } else if (telemetry.packetLossPercent() > 1) {
            score -= 8;
            reasons.add("Moderate packet loss");
        }

        if (telemetry.retryRatePercent() > 20) {
            score -= 20;
            reasons.add("High retry rate");
        } else if (telemetry.retryRatePercent() > 10) {
            score -= 8;
            reasons.add("Moderate retry rate");
        }

        score = Math.max(0, Math.min(100, score));
        return new QoeScore(deviceId, score, qoeGrade(score), reasons);
    }

    public RiskScore riskScore(String deviceId) {
        deviceService.get(deviceId);
        List<DnsDecision> decisions = stateStore.dnsDecisionsForDevice(deviceId);
        long blockedCount = decisions.stream().filter(decision -> decision.action() == DnsAction.BLOCK).count();
        int score = Math.toIntExact(Math.min(100, blockedCount * 20));

        List<String> reasons = new ArrayList<>();
        if (blockedCount == 0) {
            reasons.add("No recent blocked DNS queries");
        } else {
            reasons.add(blockedCount + " blocked DNS query/queries observed");
        }

        return new RiskScore(deviceId, score, riskGrade(score), reasons);
    }

    private ScoreGrade qoeGrade(int score) {
        if (score >= 90) {
            return ScoreGrade.EXCELLENT;
        }
        if (score >= 75) {
            return ScoreGrade.GOOD;
        }
        if (score >= 50) {
            return ScoreGrade.FAIR;
        }
        return ScoreGrade.POOR;
    }

    private ScoreGrade riskGrade(int score) {
        if (score >= 70) {
            return ScoreGrade.HIGH;
        }
        if (score >= 30) {
            return ScoreGrade.MEDIUM;
        }
        return ScoreGrade.LOW;
    }
}
