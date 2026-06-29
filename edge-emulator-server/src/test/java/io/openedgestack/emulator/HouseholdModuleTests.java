package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HouseholdModuleTests extends ModuleTestSupport {

    @Test
    void createsAndListsHouseholdsDeterministically() throws Exception {
        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "home-b",
                                  "name": "Second Demo Home",
                                  "region": "DEMO"
                                }
                                """))
                .andExpect(status().isCreated());

        createHousehold("home-a");

        mockMvc.perform(get("/households"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].householdId", is("home-a")))
                .andExpect(jsonPath("$[1].householdId", is("home-b")));
    }

    @Test
    void rejectsDuplicateHouseholdIds() throws Exception {
        createHousehold("home-001");

        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "home-001",
                                  "name": "Duplicate Demo Home",
                                  "region": "DEMO"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }
}
