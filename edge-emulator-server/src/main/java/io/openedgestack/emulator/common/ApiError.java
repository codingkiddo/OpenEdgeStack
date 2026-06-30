package io.openedgestack.emulator.common;

import java.time.Instant;

/**
 * Uniform error response body returned by all API error paths.
 *
 * <p>Every non-2xx response produced by {@link GlobalExceptionHandler} is serialised
 * into this shape so clients always have a consistent structure to parse.
 *
 * <p>Example JSON:
 * <pre>{@code
 * {
 *   "error": "NOT_FOUND",
 *   "message": "Device device-001 was not found",
 *   "timestamp": "2026-06-29T10:00:00Z"
 * }
 * }</pre>
 *
 * @param error     machine-readable error code, e.g. {@code NOT_FOUND}, {@code CONFLICT}, {@code VALIDATION_ERROR}
 * @param message   human-readable description of what went wrong
 * @param timestamp server-side UTC instant at which the error was generated
 */
public record ApiError(
        String error,
        String message,
        Instant timestamp
) {
}
