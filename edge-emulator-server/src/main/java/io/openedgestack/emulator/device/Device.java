package io.openedgestack.emulator.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Represents a network device registered under a household.
 *
 * <p>Required fields ({@code deviceId}, {@code householdId}, {@code type}) must be
 * non-blank/non-null. Optional metadata fields ({@code vendor}, {@code model},
 * {@code firmwareVersion}) may be omitted by the caller and will be {@code null}.
 *
 * @param deviceId        unique identifier for this device (required)
 * @param householdId     the owning household's ID; the household must already exist (required)
 * @param type            device category, e.g. {@code ROUTER} or {@code PHONE} (required)
 * @param vendor          manufacturer name, e.g. {@code "Netgear"} (optional)
 * @param model           model identifier used for firmware matching (optional)
 * @param firmwareVersion currently running firmware version string (optional)
 * @param status          operational state; defaults to {@link DeviceStatus#ONLINE} if omitted
 * @param createdAt       UTC instant of registration; auto-set to {@link Instant#now()} if omitted
 */
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
    /**
     * Compact constructor — supplies sensible defaults so demo devices are
     * immediately usable even when callers omit operational metadata.
     */
    public Device {
        if (status == null) {
            status = DeviceStatus.ONLINE;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
