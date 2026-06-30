package io.openedgestack.emulator.dns;

/**
 * The outcome of a DNS policy evaluation for a queried domain.
 *
 * <p>Used in {@link DnsDecision} and counted by the risk scorer:
 * every {@link #BLOCK} decision increments a device's risk score.
 */
public enum DnsAction {
    /** The domain was permitted — no matching security policy. */
    ALLOW,
    /** The domain was denied — matched a security or content policy rule. */
    BLOCK
}
