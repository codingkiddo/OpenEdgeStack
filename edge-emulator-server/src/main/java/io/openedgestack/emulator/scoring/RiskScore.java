package io.openedgestack.emulator.scoring;

import java.util.List;

public record RiskScore(
        String deviceId,
        int score,
        ScoreGrade grade,
        List<String> reasons
) {
}
