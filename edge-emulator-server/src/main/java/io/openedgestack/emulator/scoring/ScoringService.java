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

/**
 * Computes quality-of-experience (QoE) and security risk scores for devices.
 *
 * <h2>QoE scoring</h2>
 * <p>Starts at 100 and deducts points based on the device's latest Wi-Fi telemetry:
 * <ul>
 *   <li>RSSI: −25 (weak, &lt; −75 dBm) / −10 (moderate, &lt; −65 dBm)</li>
 *   <li>Latency: −25 (&gt; 150 ms) / −10 (&gt; 80 ms)</li>
 *   <li>Packet loss: −20 (&gt; 5%) / −8 (&gt; 1%)</li>
 *   <li>Retry rate: −20 (&gt; 20%) / −8 (&gt; 10%)</li>
 * </ul>
 * <p>Grade thresholds: {@link ScoreGrade#GOOD} (≥ 75), {@link ScoreGrade#FAIR} (≥ 40),
 * {@link ScoreGrade#POOR} (&lt; 40). The final score is clamped to [0, 100].
 * If no telemetry has been ingested, score 0 / {@link ScoreGrade#UNKNOWN} is returned.
 *
 * <h2>Risk scoring</h2>
 * <p>Counts blocked DNS queries in the device's history and multiplies by 20,
 * capped at 100. Grade thresholds: {@link ScoreGrade#HIGH} (≥ 70),
 * {@link ScoreGrade#MEDIUM} (≥ 30), {@link ScoreGrade#LOW} (&lt; 30).
 * A device with no blocked queries scores 0 / {@link ScoreGrade#LOW}.
 */
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

    /**
     * Computes the QoE score for a device from its latest Wi-Fi telemetry.
     *
     * @param deviceId the device to score
     * @return a {@link QoeScore} with a numeric score, grade, and per-metric reasons
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     */
    public QoeScore qoeScore(String deviceId) {
        // latestWifi validates the device exists; returns empty if no telemetry yet.
        WifiTelemetry telemetry = telemetryService.latestWifi(deviceId).orElse(null);
        if (telemetry == null) {
            // No telemetry ingested yet — cannot compute a meaningful score.
            return new QoeScore(deviceId, 0, ScoreGrade.UNKNOWN, List.of("No Wi-Fi telemetry available"));
        }

        int score = 100;
        List<String> reasons = new ArrayList<>();

        // ── RSSI deduction ────────────────────────────────────────────────────
        if (telemetry.rssi() < -75) {
            score -= 25;
            reasons.add("Weak RSSI");
        } else if (telemetry.rssi() < -65) {
            score -= 10;
            reasons.add("Moderate RSSI");
        } else {
            reasons.add("RSSI is acceptable");
        }

        // ── Latency deduction ─────────────────────────────────────────────────
        if (telemetry.latencyMs() > 150) {
            score -= 25;
            reasons.add("High latency");
        } else if (telemetry.latencyMs() > 80) {
            score -= 10;
            reasons.add("Moderate latency");
        } else {
            reasons.add("Latency is acceptable");
        }

        // ── Packet loss deduction ─────────────────────────────────────────────
        if (telemetry.packetLossPercent() > 5) {
            score -= 20;
            reasons.add("High packet loss");
        } else if (telemetry.packetLossPercent() > 1) {
            score -= 8;
            reasons.add("Moderate packet loss");
        } else {
            reasons.add("Packet loss is acceptable");
        }

        // ── Retry rate deduction ──────────────────────────────────────────────
        if (telemetry.retryRatePercent() > 20) {
            score -= 20;
            reasons.add("High retry rate");
        } else if (telemetry.retryRatePercent() > 10) {
            score -= 8;
            reasons.add("Moderate retry rate");
        } else {
            reasons.add("Retry rate is acceptable");
        }

        // Clamp to valid range in case multiple deductions push the score below 0.
        score = Math.max(0, Math.min(100, score));
        return new QoeScore(deviceId, score, qoeGrade(score), reasons);
    }

    /**
     * Computes the security risk score for a device from its DNS decision history.
     *
     * @param deviceId the device to score
     * @return a {@link RiskScore} with a numeric score, grade, and reason
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     */
    public RiskScore riskScore(String deviceId) {
        deviceService.get(deviceId);
        List<DnsDecision> decisions = stateStore.dnsDecisionsForDevice(deviceId);

        // Count only BLOCK decisions — ALLOW decisions don't increase risk.
        long blockedCount = decisions.stream().filter(decision -> decision.action() == DnsAction.BLOCK).count();

        // Each blocked query adds 20 points to the risk score, capped at 100.
        int score = Math.toIntExact(Math.min(100, blockedCount * 20));

        List<String> reasons = new ArrayList<>();
        if (blockedCount == 0) {
            reasons.add("No recent blocked DNS queries");
        } else {
            reasons.add(blockedCount + " blocked DNS query/queries observed");
        }

        return new RiskScore(deviceId, score, riskGrade(score), reasons);
    }

    /**
     * Maps a QoE numeric score to its categorical grade.
     * Grades: GOOD (≥ 75), FAIR (≥ 40), POOR (< 40).
     * UNKNOWN is returned directly by the caller when no telemetry exists.
     */
    private ScoreGrade qoeGrade(int score) {
        if (score >= 75) return ScoreGrade.GOOD;
        if (score >= 40) return ScoreGrade.FAIR;
        return ScoreGrade.POOR;
    }

    /** Maps a risk numeric score to its categorical grade. */
    private ScoreGrade riskGrade(int score) {
        if (score >= 70) return ScoreGrade.HIGH;
        if (score >= 30) return ScoreGrade.MEDIUM;
        return ScoreGrade.LOW;
    }
}
