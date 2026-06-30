package io.openedgestack.emulator.firmware;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for firmware release management and upgrade checks.
 *
 * <p>Test scenarios register firmware releases for specific device models, then
 * query the check endpoint to see whether a device needs an upgrade.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /firmware/releases}              — register a new firmware release for a model</li>
 *   <li>{@code GET  /firmware/releases?model=...}    — list all releases for a model</li>
 *   <li>{@code GET  /firmware/check?deviceId=...}    — check whether a device has a pending update</li>
 * </ul>
 */
@RestController
class FirmwareController {

    private final FirmwareService firmwareService;

    FirmwareController(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    /**
     * Registers a new firmware release.
     * Returns HTTP 201 Created. Multiple releases for the same model are accepted.
     */
    @PostMapping("/firmware/releases")
    @ResponseStatus(HttpStatus.CREATED)
    FirmwareRelease createRelease(@Valid @RequestBody FirmwareRelease release) {
        return firmwareService.createRelease(release);
    }

    /**
     * Lists all firmware releases for the given model, sorted by version ascending.
     * Returns an empty array if no releases have been registered for the model.
     */
    @GetMapping("/firmware/releases")
    List<FirmwareRelease> listReleases(@RequestParam String model) {
        return firmwareService.listReleases(model);
    }

    /**
     * Checks whether the specified device has a firmware update available.
     * Compares the device's current {@code firmwareVersion} against the latest
     * release registered for its model. Returns 404 if the device does not exist.
     */
    @GetMapping("/firmware/check")
    FirmwareCheckResponse check(@RequestParam String deviceId) {
        return firmwareService.check(deviceId);
    }
}
