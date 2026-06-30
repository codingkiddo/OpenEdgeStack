package io.openedgestack.emulator.firmware;

/**
 * Result of a firmware upgrade eligibility check for a specific device.
 *
 * <p>When no firmware release has been registered for the device's model,
 * {@code updateAvailable} is {@code false} and {@code releaseNotes} carries
 * an explanatory message instead of actual release notes.
 *
 * @param deviceId        the device that was checked
 * @param model           the device's model identifier
 * @param currentVersion  the firmware version currently running on the device
 * @param updateAvailable {@code true} if a newer release exists for this model
 * @param latestVersion   the highest version available for this model
 *                        (equals {@code currentVersion} when no update is available)
 * @param critical        {@code true} if the latest release is marked as a critical update
 * @param releaseNotes    notes from the latest release, or an advisory message if no release exists
 */
public record FirmwareCheckResponse(
        String deviceId,
        String model,
        String currentVersion,
        boolean updateAvailable,
        String latestVersion,
        boolean critical,
        String releaseNotes
) {
}
