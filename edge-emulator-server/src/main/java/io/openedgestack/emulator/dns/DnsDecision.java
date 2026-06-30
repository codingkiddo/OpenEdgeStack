package io.openedgestack.emulator.dns;

import java.time.Instant;

/**
 * The result of evaluating a DNS query against the emulator's policy rules.
 *
 * <p>Decisions are persisted in {@link io.openedgestack.emulator.common.StateStore}
 * and later read by the risk scorer to calculate a device's threat level.
 *
 * @param deviceId  the device that originated the DNS query
 * @param domain    the domain that was queried, as supplied by the caller
 * @param action    whether the domain was {@link DnsAction#ALLOW allowed} or {@link DnsAction#BLOCK blocked}
 * @param reason    machine-readable explanation, e.g. {@code "SECURITY_POLICY_MATCH"} or {@code "NO_POLICY_MATCH"}
 * @param timestamp UTC instant at which the policy decision was made
 */
public record DnsDecision(
        String deviceId,
        String domain,
        DnsAction action,
        String reason,
        Instant timestamp
) {
}
