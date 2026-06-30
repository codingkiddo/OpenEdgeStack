package io.openedgestack.emulator.scoring;

import java.util.List;

/**
 * Security risk score for a device, derived from its DNS decision history.
 *
 * <p>The score ranges from 0 (no risk) to 100 (high risk). Each blocked DNS
 * query adds 20 points, capped at 100. A score of 0 means no blocked queries
 * have been recorded since the last state reset.
 *
 * @param deviceId the device that was scored
 * @param score    numeric risk score in [0, 100]
 * @param grade    categorical label: {@link ScoreGrade#LOW}, {@link ScoreGrade#MEDIUM},
 *                 or {@link ScoreGrade#HIGH}
 * @param reasons  list of human-readable explanations describing the blocked query count
 */
public record RiskScore(
        String deviceId,
        int score,
        ScoreGrade grade,
        List<String> reasons
) {
}
