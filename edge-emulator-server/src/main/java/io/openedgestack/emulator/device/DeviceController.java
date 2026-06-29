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

@RestController
class DeviceController {

    private final DeviceService deviceService;

    DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.CREATED)
    Device register(@Valid @RequestBody Device device) {
        return deviceService.register(device);
    }

    @GetMapping("/devices/{deviceId}")
    Device get(@PathVariable String deviceId) {
        return deviceService.get(deviceId);
    }

    @GetMapping("/devices")
    List<Device> list() {
        return deviceService.list();
    }

    @GetMapping("/households/{householdId}/devices")
    List<Device> listByHousehold(@PathVariable String householdId) {
        return deviceService.listByHousehold(householdId);
    }

    @DeleteMapping("/devices/{deviceId}")
    Map<String, String> delete(@PathVariable String deviceId) {
        deviceService.delete(deviceId);
        return Map.of("status", "DELETED", "deviceId", deviceId);
    }
}
