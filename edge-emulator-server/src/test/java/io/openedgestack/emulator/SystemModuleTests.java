package io.openedgestack.emulator;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SystemModuleTests extends ModuleTestSupport {

    @Test
    void healthReturnsOpenEdgeStackStatus() throws Exception {
        mockMvc.perform(get("/_oes/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service", is("open-edge-stack")))
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
