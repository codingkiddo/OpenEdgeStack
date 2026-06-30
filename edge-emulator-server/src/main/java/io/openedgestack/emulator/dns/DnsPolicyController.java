package io.openedgestack.emulator.dns;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for DNS policy evaluation.
 *
 * <p>Exposes a single endpoint that simulates the DNS filtering layer of an
 * edge router. Each call is evaluated against built-in policy rules and the
 * resulting decision is persisted for risk scoring.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /dns/query} — evaluate a domain lookup and return the allow/block decision</li>
 * </ul>
 */
@RestController
class DnsPolicyController {

    private final DnsPolicyService dnsPolicyService;

    DnsPolicyController(DnsPolicyService dnsPolicyService) {
        this.dnsPolicyService = dnsPolicyService;
    }

    /**
     * Evaluates a DNS query against the emulator's policy rules.
     * Returns 200 OK with the {@link DnsDecision}, 400 Bad Request if fields
     * are missing, or 404 Not Found if the device does not exist.
     */
    @PostMapping("/dns/query")
    DnsDecision query(@Valid @RequestBody DnsQueryRequest request) {
        return dnsPolicyService.query(request);
    }
}
