package io.openedgestack.emulator.common;

import io.openedgestack.emulator.device.Device;
import io.openedgestack.emulator.dns.DnsDecision;
import io.openedgestack.emulator.firmware.FirmwareRelease;
import io.openedgestack.emulator.household.Household;
import io.openedgestack.emulator.telemetry.WifiTelemetry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton in-memory store that holds all emulator state.
 *
 * <p>This replaces a database for local development and testing. All collections
 * use concurrent data structures ({@link ConcurrentHashMap}, {@link CopyOnWriteArrayList})
 * so concurrent test threads don't corrupt each other's state.
 *
 * <p>Call {@link #reset()} between tests (via {@code POST /_oes/reset}) to start
 * each test case with a clean slate.
 */
@Component
public class StateStore {

    // ── Household storage ─────────────────────────────────────────────────────

    /** Households keyed by their householdId. */
    private final Map<String, Household> households = new ConcurrentHashMap<>();

    // ── Device storage ────────────────────────────────────────────────────────

    /** Devices keyed by their deviceId. */
    private final Map<String, Device> devices = new ConcurrentHashMap<>();

    // ── Per-device telemetry & DNS state ──────────────────────────────────────

    /** All Wi-Fi telemetry samples ingested, grouped by deviceId. */
    private final Map<String, List<WifiTelemetry>> wifiTelemetryByDevice = new ConcurrentHashMap<>();

    /** All DNS decisions recorded, grouped by deviceId. */
    private final Map<String, List<DnsDecision>> dnsDecisionsByDevice = new ConcurrentHashMap<>();

    // ── Firmware releases ─────────────────────────────────────────────────────

    /** Firmware releases indexed by device model string. */
    private final Map<String, List<FirmwareRelease>> firmwareReleasesByModel = new ConcurrentHashMap<>();

    // ── Household operations ──────────────────────────────────────────────────

    /**
     * Atomically inserts the household if no entry exists for its ID.
     *
     * @return {@code true} if inserted, {@code false} if the ID was already present
     */
    public boolean addHousehold(Household household) {
        return households.putIfAbsent(household.householdId(), household) == null;
    }

    /**
     * @return the household for the given ID, or {@code null} if not found
     */
    public Household household(String householdId) {
        return households.get(householdId);
    }

    /**
     * @return an immutable snapshot of all stored households (order is undefined)
     */
    public List<Household> households() {
        return List.copyOf(households.values());
    }

    /**
     * Removes the household with the given ID.
     *
     * @return {@code true} if it existed and was removed, {@code false} if not found
     */
    public boolean removeHousehold(String householdId) {
        return households.remove(householdId) != null;
    }

    // ── Device operations ─────────────────────────────────────────────────────

    /**
     * Atomically inserts the device if no entry exists for its ID.
     *
     * @return {@code true} if inserted, {@code false} if the ID was already present
     */
    public boolean addDevice(Device device) {
        return devices.putIfAbsent(device.deviceId(), device) == null;
    }

    /**
     * @return the device for the given ID, or {@code null} if not found
     */
    public Device device(String deviceId) {
        return devices.get(deviceId);
    }

    /**
     * @return an immutable snapshot of all stored devices (order is undefined)
     */
    public List<Device> devices() {
        return List.copyOf(devices.values());
    }

    /**
     * Removes the device record with the given ID.
     *
     * @return {@code true} if it existed and was removed, {@code false} if not found
     */
    public boolean removeDevice(String deviceId) {
        return devices.remove(deviceId) != null;
    }

    /**
     * Removes all telemetry and DNS state scoped to a device.
     * Called after a device is deleted so stale data doesn't accumulate.
     */
    public void removeDeviceState(String deviceId) {
        wifiTelemetryByDevice.remove(deviceId);
        dnsDecisionsByDevice.remove(deviceId);
    }

    // ── Telemetry operations ──────────────────────────────────────────────────

    /**
     * Appends a Wi-Fi telemetry sample to the device's history.
     * Multiple samples per device are supported and preserved in insertion order.
     */
    public void addWifiTelemetry(String deviceId, WifiTelemetry telemetry) {
        wifiTelemetryByDevice.computeIfAbsent(deviceId, ignored -> new CopyOnWriteArrayList<>()).add(telemetry);
    }

    /**
     * @return an immutable snapshot of all Wi-Fi telemetry for the given device,
     *         or an empty list if none has been ingested
     */
    public List<WifiTelemetry> wifiTelemetryForDevice(String deviceId) {
        return List.copyOf(wifiTelemetryByDevice.getOrDefault(deviceId, List.of()));
    }

    // ── DNS operations ────────────────────────────────────────────────────────

    /**
     * Records a DNS policy decision for a device.
     * All decisions are retained so the risk scorer can count blocked queries.
     */
    public void addDnsDecision(String deviceId, DnsDecision decision) {
        dnsDecisionsByDevice.computeIfAbsent(deviceId, ignored -> new CopyOnWriteArrayList<>()).add(decision);
    }

    /**
     * @return an immutable snapshot of all DNS decisions for the given device,
     *         or an empty list if none have been recorded
     */
    public List<DnsDecision> dnsDecisionsForDevice(String deviceId) {
        return List.copyOf(dnsDecisionsByDevice.getOrDefault(deviceId, List.of()));
    }

    // ── Firmware operations ───────────────────────────────────────────────────

    /**
     * Stores a firmware release under its device model.
     * Multiple releases per model are allowed (one per version).
     */
    public void addFirmwareRelease(String model, FirmwareRelease release) {
        firmwareReleasesByModel.computeIfAbsent(model, ignored -> new CopyOnWriteArrayList<>()).add(release);
    }

    /**
     * @return an immutable snapshot of all firmware releases for the given model,
     *         or an empty list if none have been registered
     */
    public List<FirmwareRelease> firmwareReleasesForModel(String model) {
        return List.copyOf(firmwareReleasesByModel.getOrDefault(model, List.of()));
    }

    // ── Test support ──────────────────────────────────────────────────────────

    /**
     * Clears all in-memory state.
     * Exposed via {@code POST /_oes/reset} and called in {@code @BeforeEach} in tests.
     */
    public void reset() {
        households.clear();
        devices.clear();
        wifiTelemetryByDevice.clear();
        dnsDecisionsByDevice.clear();
        firmwareReleasesByModel.clear();
    }
}
