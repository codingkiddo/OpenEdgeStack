package io.openedgestack.emulator.dns;

import jakarta.validation.constraints.NotBlank;

/**
 * Inbound request body for a DNS policy query.
 *
 * <p>Simulates the payload a device would send when resolving a domain name,
 * allowing the emulator to evaluate and log a {@link DnsDecision}.
 *
 * @param deviceId the device initiating the DNS lookup (required)
 * @param domain   the fully-qualified domain name being resolved, e.g. {@code "example.com"} (required)
 */
public record DnsQueryRequest(
        @NotBlank String deviceId,
        @NotBlank String domain
) {
}
