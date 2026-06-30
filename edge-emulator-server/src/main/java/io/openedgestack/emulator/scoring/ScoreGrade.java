package io.openedgestack.emulator.scoring;

/**
 * Categorical label applied to a numeric score to aid human interpretation.
 *
 * <p>QoE scores use {@link #EXCELLENT}, {@link #GOOD}, {@link #FAIR}, and {@link #POOR}.
 * Risk scores use {@link #HIGH}, {@link #MEDIUM}, and {@link #LOW}.
 * The two sets are intentionally combined in one enum to keep the model simple.
 *
 * <p>Grade thresholds are defined in {@link ScoringService}.
 */
public enum ScoreGrade {
    // ── QoE grades (higher score = better experience) ────────────────────────
    /** QoE score ≥ 90 — network conditions are optimal. */
    EXCELLENT,
    /** QoE score ≥ 75 — network conditions are acceptable with minor degradation. */
    GOOD,
    /** QoE score ≥ 50 — noticeable degradation; user experience may be impacted. */
    FAIR,
    /** QoE score < 50 — significant degradation; user experience is likely impacted. */
    POOR,

    // ── Risk grades (higher score = greater threat) ───────────────────────────
    /** Risk score ≥ 70 — multiple blocked DNS queries; device shows threat indicators. */
    HIGH,
    /** Risk score ≥ 30 — some blocked queries; warrants monitoring. */
    MEDIUM,
    /** Risk score < 30 — no or minimal blocked queries; device appears clean. */
    LOW
}
