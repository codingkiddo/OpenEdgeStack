package io.openedgestack.emulator.firmware;

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
