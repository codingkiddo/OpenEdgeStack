package io.openedgestack.emulator.household;

import io.openedgestack.emulator.common.ConflictException;
import io.openedgestack.emulator.common.NotFoundException;
import io.openedgestack.emulator.common.StateStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class HouseholdService {

    private final StateStore stateStore;

    public HouseholdService(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    public Household create(Household household) {
        if (!stateStore.addHousehold(household)) {
            throw new ConflictException("Household " + household.householdId() + " already exists");
        }
        return household;
    }

    public Household get(String householdId) {
        Household household = stateStore.household(householdId);
        if (household == null) {
            throw new NotFoundException("Household " + householdId + " was not found");
        }
        return household;
    }

    public List<Household> list() {
        return stateStore.households().stream()
                .sorted(Comparator.comparing(Household::householdId))
                .toList();
    }

    public void delete(String householdId) {
        if (!stateStore.removeHousehold(householdId)) {
            throw new NotFoundException("Household " + householdId + " was not found");
        }
    }
}
