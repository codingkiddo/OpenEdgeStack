package io.openedgestack.emulator.household;

import io.openedgestack.emulator.common.ConflictException;
import io.openedgestack.emulator.common.NotFoundException;
import io.openedgestack.emulator.common.StateStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Business logic for household lifecycle management.
 *
 * <p>Reads and writes through {@link StateStore}; throws typed exceptions that
 * {@code GlobalExceptionHandler} maps to appropriate HTTP status codes.
 */
@Service
public class HouseholdService {

    private final StateStore stateStore;

    public HouseholdService(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    /**
     * Registers a new household.
     *
     * @param household the household to register; {@code householdId} must be unique
     * @return the stored household (same object, with {@code createdAt} filled in)
     * @throws ConflictException if a household with the same ID already exists
     */
    public Household create(Household household) {
        if (!stateStore.addHousehold(household)) {
            throw new ConflictException("Household " + household.householdId() + " already exists");
        }
        return household;
    }

    /**
     * Retrieves a household by its ID.
     *
     * @param householdId the ID to look up
     * @return the matching household
     * @throws NotFoundException if no household exists for the given ID
     */
    public Household get(String householdId) {
        Household household = stateStore.household(householdId);
        if (household == null) {
            throw new NotFoundException("Household " + householdId + " was not found");
        }
        return household;
    }

    /**
     * Returns all households sorted alphabetically by {@code householdId}.
     * Deterministic ordering makes API responses and test assertions stable.
     */
    public List<Household> list() {
        return stateStore.households().stream()
                .sorted(Comparator.comparing(Household::householdId))
                .toList();
    }

    /**
     * Deletes a household by its ID.
     *
     * @param householdId the ID to delete
     * @throws NotFoundException if no household exists for the given ID
     */
    public void delete(String householdId) {
        if (!stateStore.removeHousehold(householdId)) {
            throw new NotFoundException("Household " + householdId + " was not found");
        }
    }
}
