package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeviceModuleTests extends ModuleTestSupport {

    @Test
    void registersAndListsDevicesForHousehold() throws Exception {
        createHousehold("home-001");
        createDevice("device-b", "home-001", "Demo-Gateway", "1.0.0");
        createDevice("device-a", "home-001", "Demo-Extender", "1.0.0");

        mockMvc.perform(get("/households/home-001/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceId", is("device-a")))
                .andExpect(jsonPath("$[1].deviceId", is("device-b")));
    }

    @Test
    void deletesDeviceAndReturnsSimpleStatus() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(delete("/devices/device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DELETED")))
                .andExpect(jsonPath("$.deviceId", is("device-001")));

        mockMvc.perform(get("/devices/device-001"))
                .andExpect(status().isNotFound());
    }
}
