package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DnsModuleTests extends ModuleTestSupport {

    @Test
    void blocksGenericSecurityTestDomains() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "domain": "malware-test.local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("BLOCK")))
                .andExpect(jsonPath("$.reason", is("SECURITY_POLICY_MATCH")));
    }

    @Test
    void allowsUnmatchedDomains() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/dns/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "domain": "example.local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("ALLOW")))
                .andExpect(jsonPath("$.reason", is("NO_POLICY_MATCH")));
    }
}
