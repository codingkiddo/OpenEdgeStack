package io.openedgestack.emulator.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralised exception-to-HTTP-response mapping for all REST controllers.
 *
 * <p>Every handler returns an {@link ApiError} body so clients always receive
 * a consistent error structure regardless of which endpoint triggered the error.
 * New domain exceptions should be added here rather than handled inline in
 * individual controllers.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Maps {@link NotFoundException} to HTTP 404.
     * Raised when a requested resource (household, device, etc.) does not exist.
     */
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", ex.getMessage(), Instant.now()));
    }

    /**
     * Maps {@link ConflictException} to HTTP 409.
     * Raised when a duplicate ID is submitted during resource creation.
     */
    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("CONFLICT", ex.getMessage(), Instant.now()));
    }

    /**
     * Maps {@link IllegalArgumentException} to HTTP 400.
     * Raised by service-layer range validations, e.g. for telemetry fields.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("BAD_REQUEST", ex.getMessage(), Instant.now()));
    }

    /**
     * Maps Bean Validation failures ({@code @NotBlank}, {@code @NotNull}, etc.) to HTTP 400.
     * The message lists every failing field so callers can fix all problems in one round trip.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("VALIDATION_ERROR", message, Instant.now()));
    }
}
