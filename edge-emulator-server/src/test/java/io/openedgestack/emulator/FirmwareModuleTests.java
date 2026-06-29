package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FirmwareModuleTests extends ModuleTestSupport {

    @Test
    void reportsFirmwareUpdateWhenNewerReleaseExists() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "model": "Demo-Gateway",
                                  "version": "1.1.0",
                                  "releaseNotes": "Fake demo maintenance release",
                                  "critical": true
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/firmware/check").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updateAvailable", is(true)))
                .andExpect(jsonPath("$.latestVersion", is("1.1.0")))
                .andExpect(jsonPath("$.critical", is(true)));
    }
}
