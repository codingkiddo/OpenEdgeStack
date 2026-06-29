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

@RestController
class HouseholdController {

    private final HouseholdService householdService;

    HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @PostMapping("/households")
    @ResponseStatus(HttpStatus.CREATED)
    Household create(@Valid @RequestBody Household household) {
        return householdService.create(household);
    }

    @GetMapping("/households/{householdId}")
    Household get(@PathVariable String householdId) {
        return householdService.get(householdId);
    }

    @GetMapping("/households")
    List<Household> list() {
        return householdService.list();
    }

    @DeleteMapping("/households/{householdId}")
    Map<String, String> delete(@PathVariable String householdId) {
        householdService.delete(householdId);
        return Map.of("status", "DELETED", "householdId", householdId);
    }
}
