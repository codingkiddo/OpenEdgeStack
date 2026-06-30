package io.openedgestack.emulator.device;

import io.openedgestack.emulator.common.ConflictException;
import io.openedgestack.emulator.common.NotFoundException;
import io.openedgestack.emulator.common.StateStore;
import io.openedgestack.emulator.household.Household;
import io.openedgestack.emulator.household.HouseholdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DeviceService}, exercising business logic directly
 * without the HTTP layer.
 *
 * <p>A fresh {@link StateStore} and real {@link HouseholdService} are wired up
 * in {@code @BeforeEach} — no mocks — so the tests exercise actual state
 * transitions. Two households ({@code home-001}, {@code home-002}) are pre-seeded
 * as test fixtures.
 */
class DeviceServiceTests {

    private StateStore stateStore;
    private DeviceService deviceService;

    /** Creates a clean store with two pre-seeded households for each test. */
    @BeforeEach
    void setUp() {
        stateStore = new StateStore();
        HouseholdService householdService = new HouseholdService(stateStore);
        householdService.create(new Household("home-001", "Demo Home", "DEMO", Instant.parse("2026-06-29T10:00:00Z")));
        householdService.create(new Household("home-002", "Second Demo Home", "DEMO", Instant.parse("2026-06-29T10:01:00Z")));
        deviceService = new DeviceService(stateStore, householdService);
    }

    @Test
    void registersDeviceWhenHouseholdExists() {
        Device device = device("device-001", "home-001", DeviceType.ROUTER);

        Device registered = deviceService.register(device);

        assertThat(registered).isEqualTo(device);
        assertThat(deviceService.get("device-001")).isEqualTo(device);
    }

    @Test
    void rejectsDeviceWhenHouseholdDoesNotExist() {
        Device device = device("device-001", "missing-home", DeviceType.ROUTER);

        assertThatThrownBy(() -> deviceService.register(device))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Household missing-home was not found");
    }

    @Test
    void rejectsDuplicateDeviceIds() {
        Device device = device("device-001", "home-001", DeviceType.ROUTER);
        deviceService.register(device);

        assertThatThrownBy(() -> deviceService.register(device))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Device device-001 already exists");
    }

    @Test
    void returnsDevicesSortedByDeviceId() {
        deviceService.register(device("device-b", "home-001", DeviceType.ROUTER));
        deviceService.register(device("device-a", "home-002", DeviceType.PHONE));

        assertThat(deviceService.list())
                .extracting(Device::deviceId)
                .containsExactly("device-a", "device-b");
    }

    @Test
    void returnsHouseholdDevicesSortedByDeviceId() {
        deviceService.register(device("device-c", "home-002", DeviceType.TABLET));
        deviceService.register(device("device-b", "home-001", DeviceType.ROUTER));
        deviceService.register(device("device-a", "home-001", DeviceType.LAPTOP));

        assertThat(deviceService.listByHousehold("home-001"))
                .extracting(Device::deviceId)
                .containsExactly("device-a", "device-b");
    }

    @Test
    void rejectsUnknownDeviceLookup() {
        assertThatThrownBy(() -> deviceService.get("missing-device"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Device missing-device was not found");
    }

    @Test
    void deletesDevice() {
        deviceService.register(device("device-001", "home-001", DeviceType.ROUTER));

        deviceService.delete("device-001");

        assertThatThrownBy(() -> deviceService.get("device-001"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Device device-001 was not found");
    }

    @Test
    void rejectsUnknownDeviceDelete() {
        assertThatThrownBy(() -> deviceService.delete("missing-device"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Device missing-device was not found");
    }

    private Device device(String deviceId, String householdId, DeviceType type) {
        return new Device(
                deviceId,
                householdId,
                type,
                "DemoVendor",
                "DemoModel",
                "1.0.0",
                DeviceStatus.ONLINE,
                Instant.parse("2026-06-29T10:00:00Z")
        );
    }
}
