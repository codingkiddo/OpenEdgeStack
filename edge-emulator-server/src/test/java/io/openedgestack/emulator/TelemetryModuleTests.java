package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TelemetryModuleTests extends ModuleTestSupport {

    @Test
    void ingestsAndListsWifiTelemetryByTimestamp() throws Exception {
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
                .andExpect(status().isOk());

        ingestWifi("device-001", -70, 45, 1.1, 7.0);

        mockMvc.perform(get("/telemetry/wifi").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].timestamp", is("2026-06-29T10:00:00Z")))
                .andExpect(jsonPath("$[1].timestamp", is("2026-06-29T10:05:00Z")));
    }

    @Test
    void rejectsOutOfRangeTelemetry() throws Exception {
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
                                  "txMbps": 40.0,
                                  "timestamp": "2026-06-29T10:05:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }
}
