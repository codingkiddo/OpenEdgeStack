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

@RestController
class FirmwareController {

    private final FirmwareService firmwareService;

    FirmwareController(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @PostMapping("/firmware/releases")
    @ResponseStatus(HttpStatus.CREATED)
    FirmwareRelease createRelease(@Valid @RequestBody FirmwareRelease release) {
        return firmwareService.createRelease(release);
    }

    @GetMapping("/firmware/releases")
    List<FirmwareRelease> listReleases(@RequestParam String model) {
        return firmwareService.listReleases(model);
    }

    @GetMapping("/firmware/check")
    FirmwareCheckResponse check(@RequestParam String deviceId) {
        return firmwareService.check(deviceId);
    }
}
