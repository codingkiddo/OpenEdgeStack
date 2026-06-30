package io.openedgestack.emulator.scoring;

import java.util.List;

/**
 * Quality-of-Experience score for a device, derived from its latest Wi-Fi telemetry.
 *
 * <p>The score ranges from 0 (worst) to 100 (best). If no telemetry has been
 * ingested, a neutral score of 50 / {@link ScoreGrade#FAIR} is returned with an
 * explanatory reason.
 *
 * @param deviceId the device that was scored
 * @param score    numeric quality score in [0, 100]
 * @param grade    categorical label corresponding to the score range
 * @param reasons  ordered list of human-readable explanations for deductions applied
 */
public record QoeScore(
        String deviceId,
        int score,
        ScoreGrade grade,
        List<String> reasons
) {
}
