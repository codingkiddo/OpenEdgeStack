package io.openedgestack.emulator.scoring;

/**
 * Categorical label applied to a numeric score to aid human interpretation.
 *
 * <p>QoE scores use {@link #GOOD}, {@link #FAIR}, {@link #POOR}, and {@link #UNKNOWN}.
 * Risk scores use {@link #HIGH}, {@link #MEDIUM}, and {@link #LOW}.
 * The two sets are intentionally combined in one enum to keep the model simple.
 *
 * <p>Grade thresholds are defined in {@link ScoringService}.
 */
public enum ScoreGrade {
    // ── QoE grades (higher score = better experience) ────────────────────────
    /** QoE score ≥ 75 — network conditions are good; minor degradation at most. */
    GOOD,
    /** QoE score ≥ 40 — noticeable degradation; user experience may be impacted. */
    FAIR,
    /** QoE score < 40 — significant degradation; user experience is likely impacted. */
    POOR,
    /** No Wi-Fi telemetry has been ingested yet; score cannot be computed. */
    UNKNOWN,

    // ── Risk grades (higher score = greater threat) ───────────────────────────
    /** Risk score ≥ 70 — multiple blocked DNS queries; device shows threat indicators. */
    HIGH,
    /** Risk score ≥ 30 — some blocked queries; warrants monitoring. */
    MEDIUM,
    /** Risk score < 30 — no or minimal blocked queries; device appears clean. */
    LOW
}
