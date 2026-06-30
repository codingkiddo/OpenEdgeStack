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
 * MockMvc integration tests for the Firmware Update Check API.
 *
 * <p>Covers release registration, release listing, and the firmware-check endpoint
 * across all meaningful scenarios: update available, already up-to-date, no releases
 * registered, unknown device, and validation errors.
 *
 * <p>No real firmware is downloaded and no external network calls are made —
 * all state lives in the in-memory {@code StateStore}.
 */
class FirmwareModuleTests extends ModuleTestSupport {

    // ── POST /firmware/releases ───────────────────────────────────────────────

    @Test
    void registersReleaseAndReturnsIt() throws Exception {
        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "model": "Demo-Gateway",
                                  "version": "1.1.0",
                                  "releaseNotes": "Stability improvements",
                                  "critical": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.model", is("Demo-Gateway")))
                .andExpect(jsonPath("$.version", is("1.1.0")))
                .andExpect(jsonPath("$.releaseNotes", is("Stability improvements")))
                .andExpect(jsonPath("$.critical", is(false)))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void rejectsMissingModel() throws Exception {
        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": "1.1.0"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("model")));
    }

    @Test
    void rejectsMissingVersion() throws Exception {
        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "model": "Demo-Gateway"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("version")));
    }

    // ── GET /firmware/releases ────────────────────────────────────────────────

    @Test
    void listsReleasesForModelSortedByVersionAscending() throws Exception {
        // Register out of order to confirm sorting is applied.
        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Gateway","version":"2.0.0","releaseNotes":"Major release","critical":false}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Gateway","version":"1.1.0","releaseNotes":"Maintenance","critical":false}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Gateway","version":"1.9.0","releaseNotes":"Feature release","critical":false}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/firmware/releases").param("model", "Demo-Gateway"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                // Numeric comparison: 1.1.0 < 1.9.0 < 2.0.0 (not lexicographic 1.1.0 < 2.0.0 < 1.9.0)
                .andExpect(jsonPath("$[0].version", is("1.1.0")))
                .andExpect(jsonPath("$[1].version", is("1.9.0")))
                .andExpect(jsonPath("$[2].version", is("2.0.0")));
    }

    @Test
    void returnsEmptyListForModelWithNoReleases() throws Exception {
        mockMvc.perform(get("/firmware/releases").param("model", "Unknown-Model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /firmware/check ───────────────────────────────────────────────────

    @Test
    void reportsFirmwareUpdateWhenNewerReleaseExists() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Gateway","version":"1.1.0","releaseNotes":"Fake demo maintenance release","critical":true}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/firmware/check").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId", is("device-001")))
                .andExpect(jsonPath("$.model", is("Demo-Gateway")))
                .andExpect(jsonPath("$.currentVersion", is("1.0.0")))
                .andExpect(jsonPath("$.updateAvailable", is(true)))
                .andExpect(jsonPath("$.latestVersion", is("1.1.0")))
                .andExpect(jsonPath("$.critical", is(true)))
                .andExpect(jsonPath("$.releaseNotes", is("Fake demo maintenance release")));
    }

    @Test
    void picksLatestVersionWhenMultipleReleasesExist() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Gateway","version":"1.9.0","releaseNotes":"Feature update","critical":false}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Gateway","version":"1.10.0","releaseNotes":"Numeric minor bump","critical":true}
                                """))
                .andExpect(status().isCreated());

        // 1.10.0 > 1.9.0 numerically — must not pick 1.9.0 due to lexicographic ordering.
        mockMvc.perform(get("/firmware/check").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestVersion", is("1.10.0")))
                .andExpect(jsonPath("$.updateAvailable", is(true)));
    }

    @Test
    void reportsNoUpdateWhenDeviceIsAlreadyOnLatestVersion() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.1.0");

        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Gateway","version":"1.1.0","releaseNotes":"Current release","critical":false}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/firmware/check").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updateAvailable", is(false)))
                .andExpect(jsonPath("$.currentVersion", is("1.1.0")))
                .andExpect(jsonPath("$.latestVersion", is("1.1.0")));
    }

    @Test
    void reportsNoUpdateWhenNoReleasesConfiguredForModel() throws Exception {
        createHousehold("home-001");
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(get("/firmware/check").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updateAvailable", is(false)))
                .andExpect(jsonPath("$.releaseNotes", containsString("No firmware release configured")));
    }

    @Test
    void returnsNotFoundForUnknownDevice() throws Exception {
        mockMvc.perform(get("/firmware/check").param("deviceId", "missing-device"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Device missing-device was not found")));
    }

    @Test
    void doesNotConfuseReleasesAcrossModels() throws Exception {
        createHousehold("home-001");
        // Device model is Demo-Gateway but we only register a release for Demo-Phone.
        createDevice("device-001", "home-001", "Demo-Gateway", "1.0.0");

        mockMvc.perform(post("/firmware/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model":"Demo-Phone","version":"2.0.0","releaseNotes":"Phone release","critical":false}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/firmware/check").param("deviceId", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updateAvailable", is(false)))
                .andExpect(jsonPath("$.releaseNotes", containsString("No firmware release configured")));
    }
}
