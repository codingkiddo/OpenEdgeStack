package io.openedgestack.emulator;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for all MockMvc integration tests.
 *
 * <p>Boots the full Spring application context once per test class (shared via
 * {@code @SpringBootTest}) and resets all in-memory state before each test method
 * via {@code POST /_oes/reset}. This gives each test a clean slate without the
 * cost of restarting the context.
 *
 * <p>Helper methods ({@link #createHousehold}, {@link #createDevice}, {@link #ingestWifi})
 * encapsulate the JSON payloads needed to seed precondition data, keeping individual
 * test methods focused on the scenario under test.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class ModuleTestSupport {

    @Autowired
    MockMvc mockMvc;

    /** Wipes all in-memory state so tests don't interfere with each other. */
    @BeforeEach
    void resetState() throws Exception {
        mockMvc.perform(post("/_oes/reset"))
                .andExpect(status().isOk());
    }

    /**
     * Creates a household with standard demo fields.
     * Use this to satisfy the household-must-exist precondition before registering devices.
     *
     * @param householdId the ID to assign to the new household
     */
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

    /**
     * Registers a device of type {@code ROUTER} under the given household.
     *
     * @param deviceId        the ID to assign to the device
     * @param householdId     the owning household (must already exist)
     * @param model           model string used for firmware-check matching
     * @param firmwareVersion the currently installed firmware version
     */
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

    /**
     * Ingests a Wi-Fi telemetry sample for a device.
     * SNR, throughput, and timestamp are fixed demo values; the parameters
     * cover the fields that affect QoE scoring.
     *
     * @param deviceId            the reporting device (must already exist)
     * @param rssi                received signal strength in dBm
     * @param latencyMs           round-trip latency in milliseconds
     * @param packetLossPercent   packet loss percentage [0, 100]
     * @param retryRatePercent    retry rate percentage [0, 100]
     */
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
