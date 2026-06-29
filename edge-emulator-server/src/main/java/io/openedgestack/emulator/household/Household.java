package io.openedgestack.emulator.household;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record Household(
        @NotBlank String householdId,
        @NotBlank String name,
        @NotBlank String region,
        Instant createdAt
) {
    public Household {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
