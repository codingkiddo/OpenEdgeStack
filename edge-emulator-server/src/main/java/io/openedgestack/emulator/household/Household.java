package io.openedgestack.emulator.household;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Represents a household — the top-level grouping entity in the edge emulator.
 *
 * <p>All devices belong to exactly one household. The household must exist before
 * any device can be registered under it.
 *
 * @param householdId unique identifier supplied by the caller (required)
 * @param name        human-readable label for the household (required)
 * @param region      optional geographic or logical region tag, e.g. {@code "EMEA"}
 * @param createdAt   UTC instant when the household was registered;
 *                    auto-set to {@link Instant#now()} if not provided by the caller
 */
public record Household(
        @NotBlank String householdId,
        @NotBlank String name,
        String region,
        Instant createdAt
) {
    /** Compact constructor — supplies a default timestamp when the caller omits it. */
    public Household {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
