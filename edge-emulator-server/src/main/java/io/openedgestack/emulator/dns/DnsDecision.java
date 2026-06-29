package io.openedgestack.emulator.dns;

import java.time.Instant;

public record DnsDecision(
        String deviceId,
        String domain,
        DnsAction action,
        String reason,
        Instant timestamp
) {
}
