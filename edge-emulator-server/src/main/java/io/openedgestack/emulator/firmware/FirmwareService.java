package io.openedgestack.emulator.firmware;

import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.device.Device;
import io.openedgestack.emulator.device.DeviceService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FirmwareService {

    private final StateStore stateStore;
    private final DeviceService deviceService;

    public FirmwareService(StateStore stateStore, DeviceService deviceService) {
        this.stateStore = stateStore;
        this.deviceService = deviceService;
    }

    public FirmwareRelease createRelease(FirmwareRelease release) {
        stateStore.addFirmwareRelease(release.model(), release);
        return release;
    }

    public List<FirmwareRelease> listReleases(String model) {
        return stateStore.firmwareReleasesForModel(model).stream()
                .sorted(Comparator.comparing(FirmwareRelease::version))
                .toList();
    }

    public FirmwareCheckResponse check(String deviceId) {
        Device device = deviceService.get(deviceId);
        FirmwareRelease latest = stateStore.firmwareReleasesForModel(device.model()).stream()
                .max(Comparator.comparing(FirmwareRelease::version))
                .orElse(null);

        if (latest == null) {
            return new FirmwareCheckResponse(
                    device.deviceId(),
                    device.model(),
                    device.firmwareVersion(),
                    false,
                    device.firmwareVersion(),
                    false,
                    "No firmware release configured for model " + device.model()
            );
        }

        boolean updateAvailable = latest.version().compareTo(device.firmwareVersion()) > 0;
        return new FirmwareCheckResponse(
                device.deviceId(),
                device.model(),
                device.firmwareVersion(),
                updateAvailable,
                latest.version(),
                latest.critical(),
                latest.releaseNotes()
        );
    }
}
