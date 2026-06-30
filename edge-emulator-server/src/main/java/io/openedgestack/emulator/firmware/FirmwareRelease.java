package io.openedgestack.emulator.firmware;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * A firmware release available for a specific device model.
 *
 * <p>Multiple releases per model are allowed and compared using lexicographic
 * version ordering (e.g. {@code "1.2.0" > "1.1.0"}). The latest version is
 * selected during firmware-check evaluations.
 *
 * @param model        the device model this release targets, e.g. {@code "Demo-Gateway"} (required)
 * @param version      semantic or comparable version string, e.g. {@code "2.1.0"} (required)
 * @param releaseNotes optional human-readable description of what changed in this release
 * @param critical     {@code true} if the update addresses a security vulnerability or critical bug
 * @param createdAt    UTC instant the release was registered; auto-set to now if omitted
 */
public record FirmwareRelease(
        @NotBlank String model,
        @NotBlank String version,
        String releaseNotes,
        boolean critical,
        Instant createdAt
) {
    /** Compact constructor — supplies the current timestamp when the caller omits it. */
    public FirmwareRelease {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
