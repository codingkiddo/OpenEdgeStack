package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc integration tests for Wi-Fi Telemetry ingestion and retrieval.
 *
 * <p>Covers {@code POST /telemetry/wifi} (ingestion + validation) and
 * {@code GET /devices/{deviceId}/telemetry/wifi/latest} (latest sample endpoint).
 * No real network calls or DNS lookups are made — all state is in-memory.
 */
class TelemetryModuleTests extends ModuleTestSupport {

    // ── POST /telemetry/wifi — happy path ─────────────────────────────────────

    @Test
    void ingestsWifiTelemetryAndReturnsAccepted() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": -61,
                                  "snr": 30,
                                  "latencyMs": 28,
                                  "packetLossPercent": 0.2,
                                  "retryRatePercent": 4.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0,
                                  "timestamp": "2026-06-29T10:05:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")))
                .andExpect(jsonPath("$.deviceId", is("device-001")))
                .andExpect(jsonPath("$.type", is("WIFI")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void ingestsAndListsMultipleSamplesSortedByTimestamp() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        // Ingest later timestamp first to confirm sorting is applied.
        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": -61,
                                  "snr": 30,
                                  "latencyMs": 28,
                                  "packetLossPercent": 0.2,
                                  "retryRatePercent": 4.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0,
                                  "timestamp": "2026-06-29T10:05:00Z"
                                }
                                """))
                .andExpect(status().isOk());

        // ingestWifi helper uses timestamp 2026-06-29T10:00:00Z (earlier).
        ingestWifi("device-001", -70, 45, 1.1, 7.0);

        mockMvc.perform(get("/telemetry/wifi").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].timestamp", is("2026-06-29T10:00:00Z")))
                .andExpect(jsonPath("$[1].timestamp", is("2026-06-29T10:05:00Z")));
    }

    @Test
    void returnsNotFoundWhenIngestingForUnknownDevice() throws Exception {
        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "missing-device",
                                  "rssi": -61,
                                  "snr": 30,
                                  "latencyMs": 28,
                                  "packetLossPercent": 0.2,
                                  "retryRatePercent": 4.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Device missing-device was not found")));
    }

    // ── POST /telemetry/wifi — field validation ───────────────────────────────

    @Test
    void rejectsPositiveRssi() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": 10,
                                  "snr": 30,
                                  "latencyMs": 28,
                                  "packetLossPercent": 0.2,
                                  "retryRatePercent": 4.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("rssi")));
    }

    @Test
    void rejectsZeroRssi() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": 0,
                                  "snr": 30,
                                  "latencyMs": 28,
                                  "packetLossPercent": 0.2,
                                  "retryRatePercent": 4.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("rssi")));
    }

    @Test
    void rejectsNegativeLatency() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": -61,
                                  "snr": 30,
                                  "latencyMs": -1,
                                  "packetLossPercent": 0.2,
                                  "retryRatePercent": 4.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("latencyMs")));
    }

    @Test
    void rejectsPacketLossAbove100() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": -61,
                                  "snr": 30,
                                  "latencyMs": 28,
                                  "packetLossPercent": 101.0,
                                  "retryRatePercent": 4.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("packetLossPercent")));
    }

    @Test
    void rejectsRetryRateAbove100() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": -61,
                                  "snr": 30,
                                  "latencyMs": 28,
                                  "packetLossPercent": 2.0,
                                  "retryRatePercent": 150.0,
                                  "rxMbps": 140.0,
                                  "txMbps": 40.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("retryRatePercent")));
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rssi": -61
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    // ── GET /devices/{deviceId}/telemetry/wifi/latest ─────────────────────────

    @Test
    void returnsLatestSampleByTimestamp() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        // Earlier sample.
        ingestWifi("device-001", -70, 45, 1.1, 7.0);

        // Later sample (explicit timestamp in the future).
        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "rssi": -55,
                                  "snr": 35,
                                  "latencyMs": 12,
                                  "packetLossPercent": 0.0,
                                  "retryRatePercent": 1.0,
                                  "rxMbps": 200.0,
                                  "txMbps": 50.0,
                                  "timestamp": "2026-06-29T11:00:00Z"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/devices/device-001/telemetry/wifi/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId", is("device-001")))
                .andExpect(jsonPath("$.rssi", is(-55)))
                .andExpect(jsonPath("$.timestamp", is("2026-06-29T11:00:00Z")));
    }

    @Test
    void returnsNotFoundForLatestWhenNoTelemetryIngested() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(get("/devices/device-001/telemetry/wifi/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("No Wi-Fi telemetry found for device device-001")));
    }

    @Test
    void returnsNotFoundForLatestWhenDeviceDoesNotExist() throws Exception {
        mockMvc.perform(get("/devices/missing-device/telemetry/wifi/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Device missing-device was not found")));
    }
}
