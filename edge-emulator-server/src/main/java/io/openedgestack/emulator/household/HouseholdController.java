package io.openedgestack.emulator.household;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Household Registry API.
 *
 * <p>Exposes CRUD endpoints under {@code /households}. Request bodies are
 * validated via Bean Validation before reaching the service layer; errors are
 * returned as {@code ApiError} JSON by {@code GlobalExceptionHandler}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST   /households}                — register a new household</li>
 *   <li>{@code GET    /households}                — list all households</li>
 *   <li>{@code GET    /households/{householdId}}  — fetch a single household</li>
 *   <li>{@code DELETE /households/{householdId}}  — remove a household</li>
 * </ul>
 */
@RestController
class HouseholdController {

    private final HouseholdService householdService;

    HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    /**
     * Registers a new household.
     * Returns HTTP 201 Created on success, 409 Conflict if the ID already exists,
     * or 400 Bad Request if required fields are missing.
     */
    @PostMapping("/households")
    @ResponseStatus(HttpStatus.CREATED)
    Household create(@Valid @RequestBody Household household) {
        return householdService.create(household);
    }

    /**
     * Fetches a single household by ID.
     * Returns HTTP 404 Not Found if the household does not exist.
     */
    @GetMapping("/households/{householdId}")
    Household get(@PathVariable String householdId) {
        return householdService.get(householdId);
    }

    /**
     * Lists all registered households sorted alphabetically by {@code householdId}.
     * Returns an empty array when no households have been registered.
     */
    @GetMapping("/households")
    List<Household> list() {
        return householdService.list();
    }

    /**
     * Deletes a household by ID and returns a simple status confirmation.
     * Returns HTTP 404 Not Found if the household does not exist.
     */
    @DeleteMapping("/households/{householdId}")
    Map<String, String> delete(@PathVariable String householdId) {
        householdService.delete(householdId);
        return Map.of("status", "DELETED", "householdId", householdId);
    }
}
