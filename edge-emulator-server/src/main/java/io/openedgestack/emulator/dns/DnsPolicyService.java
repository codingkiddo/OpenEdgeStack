package io.openedgestack.emulator.dns;

import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.device.DeviceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

/**
 * Emulated DNS policy engine.
 *
 * <p>This is a simulator only — no real DNS resolution or external network calls
 * are made. Rules are matched by domain suffix (case-insensitive) to allow
 * predictable test scenarios without any external dependencies.
 *
 * <p>Block rules evaluated in order:
 * <ol>
 *   <li>Ends with {@code "malware-test.local"}  → {@link DnsAction#BLOCK} / {@code MALWARE_DOMAIN}</li>
 *   <li>Ends with {@code "phishing-test.local"} → {@link DnsAction#BLOCK} / {@code PHISHING_DOMAIN}</li>
 *   <li>Ends with {@code "adult-test.local"}    → {@link DnsAction#BLOCK} / {@code CONTENT_POLICY}</li>
 *   <li>Everything else                         → {@link DnsAction#ALLOW} / {@code NO_MATCH}</li>
 * </ol>
 *
 * <p>Every decision is recorded in {@link StateStore} so the risk scorer can
 * count blocked queries when computing a device's threat level.
 */
@Service
public class DnsPolicyService {

    private final StateStore stateStore;
    private final DeviceService deviceService;

    public DnsPolicyService(StateStore stateStore, DeviceService deviceService) {
        this.stateStore = stateStore;
        this.deviceService = deviceService;
    }

    /**
     * Evaluates a DNS query against the policy rules and records the decision.
     *
     * @param request the query containing the device ID and domain to evaluate
     * @return a {@link DnsDecision} with {@code BLOCK} or {@code ALLOW} action and a specific reason code
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     */
    public DnsDecision query(DnsQueryRequest request) {
        // Verify the device exists before recording any decision against it.
        deviceService.get(request.deviceId());

        // Normalise to lowercase so suffix matching is case-insensitive.
        String domain = request.domain().toLowerCase(Locale.ROOT);

        String reason = evaluate(domain);
        DnsAction action = reason.equals("NO_MATCH") ? DnsAction.ALLOW : DnsAction.BLOCK;

        DnsDecision decision = new DnsDecision(request.deviceId(), request.domain(), action, reason, Instant.now());

        // Persist every decision so the risk scorer can count blocked queries.
        stateStore.addDnsDecision(request.deviceId(), decision);
        return decision;
    }

    /**
     * Applies suffix-based policy rules to the normalised domain and returns the reason code.
     *
     * @param domain lowercase domain string to evaluate
     * @return a reason code string; anything other than {@code "NO_MATCH"} implies a block
     */
    private String evaluate(String domain) {
        if (domain.endsWith("malware-test.local")) {
            return "MALWARE_DOMAIN";
        }
        if (domain.endsWith("phishing-test.local")) {
            return "PHISHING_DOMAIN";
        }
        if (domain.endsWith("adult-test.local")) {
            return "CONTENT_POLICY";
        }
        return "NO_MATCH";
    }
}
