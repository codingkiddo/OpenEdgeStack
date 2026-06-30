package io.openedgestack.emulator.device;

import io.openedgestack.emulator.common.ConflictException;
import io.openedgestack.emulator.common.NotFoundException;
import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.household.HouseholdService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Business logic for the Device Registry.
 *
 * <p>Enforces two key invariants on registration:
 * <ol>
 *   <li>The target household must already exist (referential integrity).</li>
 *   <li>The device ID must be unique across all households (global uniqueness).</li>
 * </ol>
 *
 * <p>All list methods return devices sorted by {@code deviceId} to make responses
 * and test assertions stable regardless of insertion order.
 */
@Service
public class DeviceService {

    private final StateStore stateStore;
    private final HouseholdService householdService;

    public DeviceService(StateStore stateStore, HouseholdService householdService) {
        this.stateStore = stateStore;
        this.householdService = householdService;
    }

    /**
     * Registers a new device under an existing household.
     *
     * @param device the device to register; its {@code householdId} must reference an existing household
     * @return the stored device (same object, with defaults applied by the record's compact constructor)
     * @throws NotFoundException if the referenced household does not exist
     * @throws ConflictException if a device with the same ID already exists
     */
    public Device register(Device device) {
        // Devices are always attached to a known household — reject orphaned devices early.
        householdService.get(device.householdId());
        if (!stateStore.addDevice(device)) {
            throw new ConflictException("Device " + device.deviceId() + " already exists");
        }
        return device;
    }

    /**
     * Retrieves a single device by its ID.
     *
     * @param deviceId the ID to look up
     * @return the matching device
     * @throws NotFoundException if no device exists for the given ID
     */
    public Device get(String deviceId) {
        Device device = stateStore.device(deviceId);
        if (device == null) {
            throw new NotFoundException("Device " + deviceId + " was not found");
        }
        return device;
    }

    /**
     * Returns all registered devices sorted alphabetically by {@code deviceId}.
     */
    public List<Device> list() {
        return stateStore.devices().stream()
                .sorted(Comparator.comparing(Device::deviceId))
                .toList();
    }

    /**
     * Returns all devices belonging to a specific household, sorted by {@code deviceId}.
     *
     * @param householdId the household to filter by; must exist
     * @throws NotFoundException if the household does not exist
     */
    public List<Device> listByHousehold(String householdId) {
        // Validate the household exists before filtering, so callers get 404 not an empty list.
        householdService.get(householdId);
        return stateStore.devices().stream()
                .filter(device -> device.householdId().equals(householdId))
                .sorted(Comparator.comparing(Device::deviceId))
                .toList();
    }

    /**
     * Deletes a device and all its associated emulator state (telemetry, DNS decisions).
     *
     * @param deviceId the ID of the device to delete
     * @throws NotFoundException if no device exists for the given ID
     */
    public void delete(String deviceId) {
        if (!stateStore.removeDevice(deviceId)) {
            throw new NotFoundException("Device " + deviceId + " was not found");
        }
        // Remove telemetry and DNS decisions so stale data does not influence scoring.
        stateStore.removeDeviceState(deviceId);
    }
}
