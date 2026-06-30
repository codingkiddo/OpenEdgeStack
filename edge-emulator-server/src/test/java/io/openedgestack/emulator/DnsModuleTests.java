package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc integration tests for the DNS Policy Simulation API ({@code POST /dns/query}).
 *
 * <p>Covers each suffix-based block rule, the allow path, validation errors,
 * and device-not-found. No real DNS resolution or network calls are made —
 * all decisions are derived from domain suffix matching only.
 */
class DnsModuleTests extends ModuleTestSupport {

    // ── Block rules ───────────────────────────────────────────────────────────

    @Test
    void blocksMalwareDomain() throws Exception {
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001",
                                  "domain": "malware-test.local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId", is("phone-001")))
                .andExpect(jsonPath("$.domain", is("malware-test.local")))
                .andExpect(jsonPath("$.action", is("BLOCK")))
                .andExpect(jsonPath("$.reason", is("MALWARE_DOMAIN")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void blocksMalwareDomainWithSubdomain() throws Exception {
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001",
                                  "domain": "evil.malware-test.local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("BLOCK")))
                .andExpect(jsonPath("$.reason", is("MALWARE_DOMAIN")));
    }

    @Test
    void blocksPhishingDomain() throws Exception {
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001",
                                  "domain": "phishing-test.local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("BLOCK")))
                .andExpect(jsonPath("$.reason", is("PHISHING_DOMAIN")));
    }

    @Test
    void blocksAdultContentDomain() throws Exception {
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001",
                                  "domain": "adult-test.local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("BLOCK")))
                .andExpect(jsonPath("$.reason", is("CONTENT_POLICY")));
    }

    @Test
    void matchingIsCaseInsensitive() throws Exception {
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001",
                                  "domain": "MALWARE-TEST.LOCAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("BLOCK")))
                .andExpect(jsonPath("$.reason", is("MALWARE_DOMAIN")));
    }

    // ── Allow path ────────────────────────────────────────────────────────────

    @Test
    void allowsUnmatchedDomain() throws Exception {
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001",
                                  "domain": "example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("ALLOW")))
                .andExpect(jsonPath("$.reason", is("NO_MATCH")));
    }

    @Test
    void allowsDomainThatMerelyContainsBlockedKeyword() throws Exception {
        // "malware" appearing mid-domain should NOT trigger a block — only suffix matters.
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001",
                                  "domain": "anti-malware-vendor.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("ALLOW")))
                .andExpect(jsonPath("$.reason", is("NO_MATCH")));
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Test
    void returnsNotFoundForUnknownDevice() throws Exception {
        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "missing-device",
                                  "domain": "example.com"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Device missing-device was not found")));
    }

    @Test
    void rejectsMissingDomain() throws Exception {
        createHousehold("home-001");
        createDevice("phone-001", "home-001", "Demo-Phone", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "phone-001"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("domain")));
    }

    @Test
    void rejectsMissingDeviceId() throws Exception {
        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "domain": "example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("deviceId")));
    }
}
