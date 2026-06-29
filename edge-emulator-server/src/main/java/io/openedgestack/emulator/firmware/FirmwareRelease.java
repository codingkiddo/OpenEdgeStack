package io.openedgestack.emulator.firmware;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record FirmwareRelease(
        @NotBlank String model,
        @NotBlank String version,
        String releaseNotes,
        boolean critical,
        Instant createdAt
) {
    public FirmwareRelease {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
