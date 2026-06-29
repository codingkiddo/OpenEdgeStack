package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScoringModuleTests extends ModuleTestSupport {

    @Test
    void returnsNeutralQoeScoreWithoutTelemetry() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(get("/devices/device-001/qoe-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(50)))
                .andExpect(jsonPath("$.grade", is("FAIR")));
    }

    @Test
    void scoresRiskFromBlockedDnsDecisions() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "domain": "phishing-test.local"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/devices/device-001/risk-score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(20)))
                .andExpect(jsonPath("$.grade", is("LOW")));
    }
}
