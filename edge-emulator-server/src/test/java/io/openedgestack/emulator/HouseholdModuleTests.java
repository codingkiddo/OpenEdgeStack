package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HouseholdModuleTests extends ModuleTestSupport {

    @Test
    void createsHouseholdWithRequiredFields() throws Exception {
        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "home-001",
                                  "name": "Demo Home",
                                  "region": "DEMO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.householdId", is("home-001")))
                .andExpect(jsonPath("$.name", is("Demo Home")))
                .andExpect(jsonPath("$.region", is("DEMO")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void createsHouseholdWithoutOptionalRegion() throws Exception {
        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "home-001",
                                  "name": "Demo Home"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.householdId", is("home-001")))
                .andExpect(jsonPath("$.name", is("Demo Home")))
                .andExpect(jsonPath("$.region", nullValue()));
    }

    @Test
    void getsHouseholdById() throws Exception {
        createHousehold("home-001");

        mockMvc.perform(get("/households/home-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.householdId", is("home-001")))
                .andExpect(jsonPath("$.name", is("Demo Home")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void listsHouseholdsDeterministically() throws Exception {
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
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("Household home-001 already exists")));
    }

    @Test
    void rejectsMissingHouseholdId() throws Exception {
        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Demo Home",
                                  "region": "DEMO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("householdId")));
    }

    @Test
    void rejectsMissingName() throws Exception {
        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "householdId": "home-001",
                                  "region": "DEMO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("name")));
    }

    @Test
    void returnsNotFoundForUnknownHousehold() throws Exception {
        mockMvc.perform(get("/households/missing-home"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Household missing-home was not found")));
    }

    @Test
    void deletesHouseholdById() throws Exception {
        createHousehold("home-001");

        mockMvc.perform(delete("/households/home-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DELETED")))
                .andExpect(jsonPath("$.householdId", is("home-001")));

        mockMvc.perform(get("/households/home-001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownHousehold() throws Exception {
        mockMvc.perform(delete("/households/missing-home"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Household missing-home was not found")));
    }
}
