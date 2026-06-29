package io.openedgestack.emulator.device;

import io.openedgestack.emulator.common.ConflictException;
import io.openedgestack.emulator.common.NotFoundException;
import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.household.HouseholdService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DeviceService {

    private final StateStore stateStore;
    private final HouseholdService householdService;

    public DeviceService(StateStore stateStore, HouseholdService householdService) {
        this.stateStore = stateStore;
        this.householdService = householdService;
    }

    public Device register(Device device) {
        householdService.get(device.householdId());
        if (!stateStore.addDevice(device)) {
            throw new ConflictException("Device " + device.deviceId() + " already exists");
        }
        return device;
    }

    public Device get(String deviceId) {
        Device device = stateStore.device(deviceId);
        if (device == null) {
            throw new NotFoundException("Device " + deviceId + " was not found");
        }
        return device;
    }

    public List<Device> list() {
        return stateStore.devices().stream()
                .sorted(Comparator.comparing(Device::deviceId))
                .toList();
    }

    public List<Device> listByHousehold(String householdId) {
        householdService.get(householdId);
        return stateStore.devices().stream()
                .filter(device -> device.householdId().equals(householdId))
                .sorted(Comparator.comparing(Device::deviceId))
                .toList();
    }

    public void delete(String deviceId) {
        if (!stateStore.removeDevice(deviceId)) {
            throw new NotFoundException("Device " + deviceId + " was not found");
        }
        stateStore.removeDeviceState(deviceId);
    }
}
