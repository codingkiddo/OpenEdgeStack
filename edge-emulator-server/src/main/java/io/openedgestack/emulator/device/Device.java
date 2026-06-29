package io.openedgestack.emulator.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record Device(
        @NotBlank String deviceId,
        @NotBlank String householdId,
        @NotNull DeviceType type,
        String vendor,
        String model,
        String firmwareVersion,
        DeviceStatus status,
        Instant createdAt
) {
    public Device {
        if (status == null) {
            status = DeviceStatus.ONLINE;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
