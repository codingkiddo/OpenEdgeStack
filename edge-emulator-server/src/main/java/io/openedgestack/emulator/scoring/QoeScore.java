package io.openedgestack.emulator.scoring;

import java.util.List;

public record QoeScore(
        String deviceId,
        int score,
        ScoreGrade grade,
        List<String> reasons
) {
}
