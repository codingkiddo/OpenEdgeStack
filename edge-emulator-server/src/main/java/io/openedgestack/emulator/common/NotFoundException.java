package io.openedgestack.emulator.common;

/**
 * Thrown when a requested resource does not exist in the in-memory state store.
 *
 * <p>{@link GlobalExceptionHandler} maps this exception to HTTP 404 Not Found
 * with an {@link ApiError} body whose {@code error} field is {@code "NOT_FOUND"}.
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message human-readable description identifying which resource was not found,
     *                e.g. {@code "Device device-001 was not found"}
     */
    public NotFoundException(String message) {
        super(message);
    }
}
