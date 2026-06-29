package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeviceModuleTests extends ModuleTestSupport {

    @Test
    void registersDeviceWithExpectedFields() throws Exception {
        createHousehold("home-001");

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "householdId": "home-001",
                                  "type": "ROUTER",
                                  "vendor": "DemoVendor",
                                  "model": "Demo-Gateway",
                                  "firmwareVersion": "1.0.0",
                                  "status": "OFFLINE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId", is("device-001")))
                .andExpect(jsonPath("$.householdId", is("home-001")))
                .andExpect(jsonPath("$.type", is("ROUTER")))
                .andExpect(jsonPath("$.vendor", is("DemoVendor")))
                .andExpect(jsonPath("$.model", is("Demo-Gateway")))
                .andExpect(jsonPath("$.firmwareVersion", is("1.0.0")))
                .andExpect(jsonPath("$.status", is("OFFLINE")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void defaultsDeviceStatusAndAllowsOptionalMetadata() throws Exception {
        createHousehold("home-001");

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "householdId": "home-001",
                                  "type": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId", is("device-001")))
                .andExpect(jsonPath("$.status", is("ONLINE")))
                .andExpect(jsonPath("$.vendor", nullValue()))
                .andExpect(jsonPath("$.model", nullValue()))
                .andExpect(jsonPath("$.firmwareVersion", nullValue()));
    }

    @Test
    void getsDeviceById() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(get("/devices/device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId", is("device-001")))
                .andExpect(jsonPath("$.householdId", is("home-001")))
                .andExpect(jsonPath("$.type", is("ROUTER")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void listsAllDevicesDeterministically() throws Exception {
        createHousehold("home-001");
        createHousehold("home-002");
        createDevice("device-b", "home-001", "Demo-Gateway", "1.0.0");
        createDevice("device-a", "home-002", "Demo-Phone", "1.0.0");

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceId", is("device-a")))
                .andExpect(jsonPath("$[1].deviceId", is("device-b")));
    }

    @Test
    void listsDevicesForHouseholdDeterministically() throws Exception {
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
    void rejectsRegistrationForUnknownHousehold() throws Exception {
        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "householdId": "missing-home",
                                  "type": "ROUTER"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Household missing-home was not found")));
    }

    @Test
    void rejectsDuplicateDeviceIds() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "householdId": "home-001",
                                  "type": "ROUTER"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("Device device-001 already exists")));
    }

    @Test
    void rejectsMissingRequiredDeviceId() throws Exception {
        createHousehold("home-001");

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "home-001",
                                  "type": "ROUTER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("deviceId")));
    }

    @Test
    void rejectsMissingRequiredHouseholdId() throws Exception {
        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "type": "ROUTER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("householdId")));
    }

    @Test
    void rejectsMissingRequiredType() throws Exception {
        createHousehold("home-001");

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "device-001",
                                  "householdId": "home-001"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("type")));
    }

    @Test
    void returnsNotFoundForUnknownDevice() throws Exception {
        mockMvc.perform(get("/devices/missing-device"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Device missing-device was not found")));
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

    @Test
    void returnsNotFoundWhenDeletingUnknownDevice() throws Exception {
        mockMvc.perform(delete("/devices/missing-device"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Device missing-device was not found")));
    }
}
