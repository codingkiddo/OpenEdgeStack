package io.openedgestack.emulator.common;

import java.time.Instant;

public record ApiError(
        String error,
        String message,
        Instant timestamp
) {
}
