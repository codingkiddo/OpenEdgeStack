package io.openedgestack.emulator.dns;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DnsPolicyController {

    private final DnsPolicyService dnsPolicyService;

    DnsPolicyController(DnsPolicyService dnsPolicyService) {
        this.dnsPolicyService = dnsPolicyService;
    }

    @PostMapping("/dns/query")
    DnsDecision query(@Valid @RequestBody DnsQueryRequest request) {
        return dnsPolicyService.query(request);
    }
}
