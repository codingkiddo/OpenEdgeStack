package io.openedgestack.emulator.dns;

import jakarta.validation.constraints.NotBlank;

public record DnsQueryRequest(
        @NotBlank String deviceId,
        @NotBlank String domain
) {
}
