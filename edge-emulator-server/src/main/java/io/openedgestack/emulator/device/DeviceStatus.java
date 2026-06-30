package io.openedgestack.emulator.device;

/**
 * Operational status of a registered device.
 *
 * <p>Defaults to {@link #ONLINE} when a device is registered without an explicit status,
 * so demo devices are immediately usable without extra setup.
 */
public enum DeviceStatus {
    /** Device is reachable and operating normally. */
    ONLINE,
    /** Device is known but currently unreachable. */
    OFFLINE,
    /** Device status has not been determined. */
    UNKNOWN
}
