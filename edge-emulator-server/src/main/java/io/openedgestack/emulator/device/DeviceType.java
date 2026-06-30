package io.openedgestack.emulator.device;

/**
 * Classification of a registered device by its primary function.
 *
 * <p>Used for display, filtering, and future policy decisions.
 * {@code UNKNOWN} acts as a safe default when the caller cannot determine the type.
 */
public enum DeviceType {
    /** Home or ISP-provided Wi-Fi router. */
    ROUTER,
    /** Network gateway / modem combo device. */
    GATEWAY,
    /** Mobile phone or smartphone. */
    PHONE,
    /** Laptop or notebook computer. */
    LAPTOP,
    /** Television or streaming display device. */
    SMART_TV,
    /** IoT-class security or surveillance camera. */
    IOT_CAMERA,
    /** Games console, e.g. PlayStation, Xbox, Switch. */
    GAMING_CONSOLE,
    /** Tablet computer. */
    TABLET,
    /** Device type could not be determined by the caller. */
    UNKNOWN
}
