package io.openedgestack.emulator;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class ModuleTestSupport {

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void resetState() throws Exception {
        mockMvc.perform(post("/_oes/reset"))
                .andExpect(status().isOk());
    }

    void createHousehold(String householdId) throws Exception {
        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "%s",
                                  "name": "Demo Home",
                                  "region": "DEMO"
                                }
                                """.formatted(householdId)))
                .andExpect(status().isCreated());
    }

    void createDevice(String deviceId, String householdId, String model, String firmwareVersion) throws Exception {
        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "%s",
                                  "householdId": "%s",
                                  "type": "ROUTER",
                                  "vendor": "DemoVendor",
                                  "model": "%s",
                                  "firmwareVersion": "%s"
                                }
                                """.formatted(deviceId, householdId, model, firmwareVersion)))
                .andExpect(status().isCreated());
    }

    void ingestWifi(String deviceId, int rssi, int latencyMs, double packetLossPercent, double retryRatePercent) throws Exception {
        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "%s",
                                  "rssi": %d,
                                  "snr": 24,
                                  "latencyMs": %d,
                                  "packetLossPercent": %.1f,
                                  "retryRatePercent": %.1f,
                                  "rxMbps": 100.0,
                                  "txMbps": 30.0,
                                  "timestamp": "2026-06-29T10:00:00Z"
                                }
                                """.formatted(deviceId, rssi, latencyMs, packetLossPercent, retryRatePercent)))
                .andExpect(status().isOk());
    }
}
