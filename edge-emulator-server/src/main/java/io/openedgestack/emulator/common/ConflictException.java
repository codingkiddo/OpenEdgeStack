package io.openedgestack.emulator.common;

/**
 * Thrown when an operation would violate a uniqueness constraint,
 * such as registering a duplicate {@code householdId} or {@code deviceId}.
 *
 * <p>{@link GlobalExceptionHandler} maps this exception to HTTP 409 Conflict
 * with an {@link ApiError} body whose {@code error} field is {@code "CONFLICT"}.
 */
public class ConflictException extends RuntimeException {

    /**
     * @param message human-readable description of the conflict,
     *                e.g. {@code "Household home-001 already exists"}
     */
    public ConflictException(String message) {
        super(message);
    }
}
