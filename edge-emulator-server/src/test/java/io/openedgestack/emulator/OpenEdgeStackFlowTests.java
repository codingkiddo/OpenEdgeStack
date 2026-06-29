package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenEdgeStackFlowTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldRunBasicDeviceIntelligenceFlow() throws Exception {
        mockMvc.perform(post("/_oes/reset"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "home-001",
                                  "name": "Demo Home",
                                  "region": "IN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.householdId", is("home-001")));

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "router-001",
                                  "householdId": "home-001",
                                  "type": "ROUTER",
                                  "vendor": "OpenEdge",
                                  "model": "OES-Gateway-1",
                                  "firmwareVersion": "1.0.0"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId", is("router-001")));

        mockMvc.perform(post("/telemetry/wifi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "router-001",
                                  "rssi": -68,
                                  "snr": 25,
                                  "latencyMs": 42,
                                  "packetLossPercent": 1.5,
                                  "retryRatePercent": 8.2,
                                  "rxMbps": 120,
                                  "txMbps": 35,
                                  "timestamp": "2026-06-29T10:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));

        mockMvc.perform(get("/devices/router-001/qoe-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId", is("router-001")));

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "router-001",
                                  "domain": "malware-test.local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("BLOCK")));
    }
}
