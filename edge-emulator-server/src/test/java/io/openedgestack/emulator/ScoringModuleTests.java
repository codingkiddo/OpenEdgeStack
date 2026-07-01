package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc integration tests for the QoE and Risk scoring APIs.
 *
 * <p>QoE scoring is deterministic: grades and scores are derived entirely from
 * the Wi-Fi telemetry values supplied in each test. Risk scoring is derived
 * from blocked DNS decisions. No ML, no AI, no randomness.
 *
 * <p>Score derivations are documented inline so the expected values can be
 * verified by hand.
 */
class ScoringModuleTests extends ModuleTestSupport {

    // ── QoE scoring ───────────────────────────────────────────────────────────

    @Test
    void returnsUnknownQoeGradeWhenNoTelemetryIngested() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(get("/devices/device-001/qoe-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(0)))
                .andExpect(jsonPath("$.grade", is("UNKNOWN")))
                .andExpect(jsonPath("$.reasons[0]", containsString("No Wi-Fi telemetry available")));
    }

    @Test
    void returnsGoodQoeGradeForExcellentSignalConditions() throws Exception {
        // Derivation: rssi=-60 (0), latency=50ms (0), loss=0% (0), retry=3% (0) → 100 → GOOD
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        ingestWifi("device-001", -60, 50, 0.0, 3.0);

        mockMvc.perform(get("/devices/device-001/qoe-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(100)))
                .andExpect(jsonPath("$.grade", is("GOOD")))
                .andExpect(jsonPath("$.reasons", hasItem("RSSI is acceptable")))
                .andExpect(jsonPath("$.reasons", hasItem("Latency is acceptable")));
    }

    @Test
    void returnsFairQoeGradeForModerateSignalDegradation() throws Exception {
        // Derivation: rssi=-70 (−10), latency=100ms (−10), loss=2% (−8), retry=5% (0) → 72 → FAIR
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        ingestWifi("device-001", -70, 100, 2.0, 5.0);

        mockMvc.perform(get("/devices/device-001/qoe-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(72)))
                .andExpect(jsonPath("$.grade", is("FAIR")))
                .andExpect(jsonPath("$.reasons", hasItem("Moderate RSSI")))
                .andExpect(jsonPath("$.reasons", hasItem("Moderate latency")))
                .andExpect(jsonPath("$.reasons", hasItem("Moderate packet loss")));
    }

    @Test
    void returnsPoorQoeGradeForSevereSignalDegradation() throws Exception {
        // Derivation: rssi=-80 (−25), latency=200ms (−25), loss=6% (−20), retry=25% (−20) → 10 → POOR
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        ingestWifi("device-001", -80, 200, 6.0, 25.0);

        mockMvc.perform(get("/devices/device-001/qoe-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(10)))
                .andExpect(jsonPath("$.grade", is("POOR")))
                .andExpect(jsonPath("$.reasons", hasItem("Weak RSSI")))
                .andExpect(jsonPath("$.reasons", hasItem("High latency")))
                .andExpect(jsonPath("$.reasons", hasItem("High packet loss")))
                .andExpect(jsonPath("$.reasons", hasItem("High retry rate")));
    }

    @Test
    void qoeScoreIsBasedOnLatestTelemetrySample() throws Exception {
        // First sample is poor; second sample is good — score should reflect the latest.
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        // Earlier poor sample (timestamp 10:00).
        ingestWifi("device-001", -80, 200, 6.0, 25.0);

        // Later good sample (timestamp 11:00).
        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": -60,
                                  "snr": 35,
                                  "latencyMs": 20,
                                  "packetLossPercent": 0.0,
                                  "retryRatePercent": 1.0,
                                  "rxMbps": 200.0,
                                  "txMbps": 50.0,
                                  "timestamp": "2026-06-29T11:00:00Z"
                                }
                                """))
                .andExpect(status().isOk());

        // Score should be 100 (GOOD), not POOR.
        mockMvc.perform(get("/devices/device-001/qoe-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(100)))
                .andExpect(jsonPath("$.grade", is("GOOD")));
    }

    @Test
    void returnsNotFoundQoeScoreForUnknownDevice() throws Exception {
        mockMvc.perform(get("/devices/missing-device/qoe-score"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    // ── Risk scoring ──────────────────────────────────────────────────────────

    @Test
    void returnsLowRiskWhenNoDnsActivityExists() throws Exception {
        // Derivation: 0 blocked queries → score=0 → LOW
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(get("/devices/device-001/risk-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(0)))
                .andExpect(jsonPath("$.grade", is("LOW")))
                .andExpect(jsonPath("$.reasons[0]", containsString("No recent blocked DNS queries")));
    }

    @Test
    void returnsLowRiskWhenAllDnsQueriesAreAllowed() throws Exception {
        // ALLOW decisions do not add to risk — score stays 0.
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        dnsQuery("device-001", "example.com");
        dnsQuery("device-001", "google.com");

        mockMvc.perform(get("/devices/device-001/risk-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(0)))
                .andExpect(jsonPath("$.grade", is("LOW")));
    }

    @Test
    void returnsMediumRiskForSomeBlockedDnsQueries() throws Exception {
        // Derivation: 2 blocked queries × 20 = 40 → MEDIUM (≥ 30)
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        dnsQuery("device-001", "malware-test.local");      // BLOCK
        dnsQuery("device-001", "phishing-test.local");     // BLOCK
        dnsQuery("device-001", "safe-site.com");           // ALLOW

        mockMvc.perform(get("/devices/device-001/risk-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(40)))
                .andExpect(jsonPath("$.grade", is("MEDIUM")));
    }

    @Test
    void returnsHighRiskForManyBlockedDnsQueries() throws Exception {
        // Derivation: 4 blocked queries × 20 = 80 → HIGH (≥ 70)
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        dnsQuery("device-001", "malware-test.local");
        dnsQuery("device-001", "phishing-test.local");
        dnsQuery("device-001", "adult-test.local");
        dnsQuery("device-001", "evil.malware-test.local");

        mockMvc.perform(get("/devices/device-001/risk-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(80)))
                .andExpect(jsonPath("$.grade", is("HIGH")))
                .andExpect(jsonPath("$.reasons[0]", containsString("4 blocked")));
    }

    @Test
    void riskScoreIsCappedAt100() throws Exception {
        // 6 blocks × 20 = 120, capped at 100.
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        for (int i = 0; i < 6; i++) {
            dnsQuery("device-001", "malware-test.local");
        }

        mockMvc.perform(get("/devices/device-001/risk-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(100)))
                .andExpect(jsonPath("$.grade", is("HIGH")));
    }

    @Test
    void returnsNotFoundRiskScoreForUnknownDevice() throws Exception {
        mockMvc.perform(get("/devices/missing-device/risk-score"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Posts a DNS query on behalf of a device. Used to seed risk-score preconditions. */
    private void dnsQuery(String deviceId, String domain) throws Exception {
        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "%s",
                                  "domain": "%s"
                                }
                                """.formatted(deviceId, domain)))
                .andExpect(status().isOk());
    }
}
