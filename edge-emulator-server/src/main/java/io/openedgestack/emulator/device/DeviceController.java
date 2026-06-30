package io.openedgestack.emulator.device;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Device Registry API.
 *
 * <p>Exposes CRUD endpoints for devices under {@code /devices} and a
 * household-scoped list under {@code /households/{householdId}/devices}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST   /devices}                                — register a new device</li>
 *   <li>{@code GET    /devices}                                — list all devices</li>
 *   <li>{@code GET    /devices/{deviceId}}                     — fetch a single device</li>
 *   <li>{@code GET    /households/{householdId}/devices}       — list devices for a household</li>
 *   <li>{@code DELETE /devices/{deviceId}}                     — remove a device</li>
 * </ul>
 */
@RestController
class DeviceController {

    private final DeviceService deviceService;

    DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * Registers a new device.
     * Returns HTTP 201 Created on success, 404 Not Found if the household is unknown,
     * 409 Conflict if the device ID already exists, or 400 Bad Request for validation failures.
     */
    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.CREATED)
    Device register(@Valid @RequestBody Device device) {
        return deviceService.register(device);
    }

    /**
     * Fetches a single device by its ID.
     * Returns HTTP 404 Not Found if the device does not exist.
     */
    @GetMapping("/devices/{deviceId}")
    Device get(@PathVariable String deviceId) {
        return deviceService.get(deviceId);
    }

    /**
     * Lists all registered devices across all households, sorted by {@code deviceId}.
     * Returns an empty array when no devices have been registered.
     */
    @GetMapping("/devices")
    List<Device> list() {
        return deviceService.list();
    }

    /**
     * Lists all devices belonging to the specified household, sorted by {@code deviceId}.
     * Returns HTTP 404 Not Found if the household does not exist.
     */
    @GetMapping("/households/{householdId}/devices")
    List<Device> listByHousehold(@PathVariable String householdId) {
        return deviceService.listByHousehold(householdId);
    }

    /**
     * Deletes a device and its associated state (telemetry, DNS decisions).
     * Returns a simple status confirmation object, or HTTP 404 if the device is unknown.
     */
    @DeleteMapping("/devices/{deviceId}")
    Map<String, String> delete(@PathVariable String deviceId) {
        deviceService.delete(deviceId);
        return Map.of("status", "DELETED", "deviceId", deviceId);
    }
}
