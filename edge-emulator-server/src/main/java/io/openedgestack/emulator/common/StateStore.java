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

@Component
public class StateStore {

    private final Map<String, Household> households = new ConcurrentHashMap<>();
    private final Map<String, Device> devices = new ConcurrentHashMap<>();
    private final Map<String, List<WifiTelemetry>> wifiTelemetryByDevice = new ConcurrentHashMap<>();
    private final Map<String, List<DnsDecision>> dnsDecisionsByDevice = new ConcurrentHashMap<>();
    private final Map<String, List<FirmwareRelease>> firmwareReleasesByModel = new ConcurrentHashMap<>();

    public boolean addHousehold(Household household) {
        return households.putIfAbsent(household.householdId(), household) == null;
    }

    public Household household(String householdId) {
        return households.get(householdId);
    }

    public List<Household> households() {
        return List.copyOf(households.values());
    }

    public boolean addDevice(Device device) {
        return devices.putIfAbsent(device.deviceId(), device) == null;
    }

    public Device device(String deviceId) {
        return devices.get(deviceId);
    }

    public List<Device> devices() {
        return List.copyOf(devices.values());
    }

    public boolean removeDevice(String deviceId) {
        return devices.remove(deviceId) != null;
    }

    public void removeDeviceState(String deviceId) {
        wifiTelemetryByDevice.remove(deviceId);
        dnsDecisionsByDevice.remove(deviceId);
    }

    public void addWifiTelemetry(String deviceId, WifiTelemetry telemetry) {
        wifiTelemetryByDevice.computeIfAbsent(deviceId, ignored -> new CopyOnWriteArrayList<>()).add(telemetry);
    }

    public List<WifiTelemetry> wifiTelemetryForDevice(String deviceId) {
        return List.copyOf(wifiTelemetryByDevice.getOrDefault(deviceId, List.of()));
    }

    public void addDnsDecision(String deviceId, DnsDecision decision) {
        dnsDecisionsByDevice.computeIfAbsent(deviceId, ignored -> new CopyOnWriteArrayList<>()).add(decision);
    }

    public List<DnsDecision> dnsDecisionsForDevice(String deviceId) {
        return List.copyOf(dnsDecisionsByDevice.getOrDefault(deviceId, List.of()));
    }

    public void addFirmwareRelease(String model, FirmwareRelease release) {
        firmwareReleasesByModel.computeIfAbsent(model, ignored -> new CopyOnWriteArrayList<>()).add(release);
    }

    public List<FirmwareRelease> firmwareReleasesForModel(String model) {
        return List.copyOf(firmwareReleasesByModel.getOrDefault(model, List.of()));
    }

    public void reset() {
        households.clear();
        devices.clear();
        wifiTelemetryByDevice.clear();
        dnsDecisionsByDevice.clear();
        firmwareReleasesByModel.clear();
    }
}
