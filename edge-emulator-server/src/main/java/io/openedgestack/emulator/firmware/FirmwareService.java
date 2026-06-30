package io.openedgestack.emulator.firmware;

import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.device.Device;
import io.openedgestack.emulator.device.DeviceService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Business logic for firmware release management and upgrade eligibility checks.
 *
 * <p>Version strings are expected to follow {@code major.minor.patch} semver format
 * (e.g. {@code "1.0.0"}, {@code "1.10.0"}, {@code "2.0.0"}). Comparison is done
 * numerically per component so that {@code 1.10.0 > 1.9.0} — lexicographic
 * string ordering would give the wrong result in that case.
 */
@Service
public class FirmwareService {

    private final StateStore stateStore;
    private final DeviceService deviceService;

    public FirmwareService(StateStore stateStore, DeviceService deviceService) {
        this.stateStore = stateStore;
        this.deviceService = deviceService;
    }

    /**
     * Stores a firmware release for the given model.
     * No deduplication is enforced — the same version can be registered multiple times.
     *
     * @param release the release to store
     * @return the same release, with {@code createdAt} defaulted if it was null
     */
    public FirmwareRelease createRelease(FirmwareRelease release) {
        stateStore.addFirmwareRelease(release.model(), release);
        return release;
    }

    /**
     * Returns all firmware releases for the given model, sorted by version ascending
     * using numeric semver comparison.
     *
     * @param model the device model to filter by
     * @return ordered list of releases; empty if none have been registered for the model
     */
    public List<FirmwareRelease> listReleases(String model) {
        return stateStore.firmwareReleasesForModel(model).stream()
                .sorted(Comparator.comparing(FirmwareRelease::version, FirmwareService::compareVersions))
                .toList();
    }

    /**
     * Checks whether a firmware upgrade is available for the given device.
     *
     * <p>Resolves the device's model, finds the highest-versioned release registered
     * for that model, and compares it numerically against the device's current version.
     * If no release has been registered, {@code updateAvailable} is {@code false} and
     * {@code releaseNotes} carries an explanatory message.
     *
     * @param deviceId the device to evaluate
     * @return a {@link FirmwareCheckResponse} describing the device's update status
     * @throws io.openedgestack.emulator.common.NotFoundException if the device does not exist
     */
    public FirmwareCheckResponse check(String deviceId) {
        Device device = deviceService.get(deviceId);

        // Find the latest release for this model using numeric version ordering.
        FirmwareRelease latest = stateStore.firmwareReleasesForModel(device.model()).stream()
                .max(Comparator.comparing(FirmwareRelease::version, FirmwareService::compareVersions))
                .orElse(null);

        // No releases registered for this model — return a neutral response rather than 404.
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

        // An update is available only when the latest registered version is strictly newer.
        boolean updateAvailable = compareVersions(latest.version(), device.firmwareVersion()) > 0;
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

    /**
     * Compares two {@code major.minor.patch} version strings numerically.
     *
     * <p>Each component is parsed as an integer so that {@code 1.10.0 > 1.9.0},
     * which lexicographic ordering gets wrong. Components beyond the first three
     * are ignored; missing components default to zero.
     *
     * @return negative if {@code a < b}, zero if equal, positive if {@code a > b}
     */
    static int compareVersions(String a, String b) {
        String[] partsA = a.split("\\.", -1);
        String[] partsB = b.split("\\.", -1);
        int length = Math.max(partsA.length, partsB.length);
        for (int i = 0; i < length; i++) {
            int numA = i < partsA.length ? Integer.parseInt(partsA[i]) : 0;
            int numB = i < partsB.length ? Integer.parseInt(partsB[i]) : 0;
            if (numA != numB) {
                return Integer.compare(numA, numB);
            }
        }
        return 0;
    }
}
