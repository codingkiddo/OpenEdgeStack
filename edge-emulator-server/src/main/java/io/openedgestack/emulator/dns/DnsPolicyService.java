package io.openedgestack.emulator.dns;

import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.device.DeviceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
public class DnsPolicyService {

    private final StateStore stateStore;
    private final DeviceService deviceService;

    public DnsPolicyService(StateStore stateStore, DeviceService deviceService) {
        this.stateStore = stateStore;
        this.deviceService = deviceService;
    }

    public DnsDecision query(DnsQueryRequest request) {
        deviceService.get(request.deviceId());

        String domain = request.domain().toLowerCase(Locale.ROOT);
        DnsDecision decision;
        if (domain.contains("malware") || domain.contains("phishing") || domain.endsWith(".blocked.local")) {
            decision = new DnsDecision(request.deviceId(), request.domain(), DnsAction.BLOCK, "SECURITY_POLICY_MATCH", Instant.now());
        } else {
            decision = new DnsDecision(request.deviceId(), request.domain(), DnsAction.ALLOW, "NO_POLICY_MATCH", Instant.now());
        }

        stateStore.addDnsDecision(request.deviceId(), decision);
        return decision;
    }
}
