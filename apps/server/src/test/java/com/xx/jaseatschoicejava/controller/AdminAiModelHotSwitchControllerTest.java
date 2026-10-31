package com.xx.jaseatschoicejava.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;

class AdminAiModelHotSwitchControllerTest {

    private AiRuntimeConfigService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(AiRuntimeConfigService.class);
        AdminAiConfigController controller = new AdminAiConfigController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private AiModelCandidateDTO candidate() {
        return AiModelCandidateDTO.builder()
            .provider("zhipu").model("glm-4-flash")
            .supervisorModel("glm-4-flash").visionModel("glm-4.6v-flash")
            .baseUrl("").timeoutMs(30000).temperature(0.7).build();
    }

    @Test
    void candidateTestIsExposedAndDoesNotPersist() throws Exception {
        when(service.testCandidateConnectivity(Mockito.any(AiModelCandidateDTO.class)))
            .thenReturn(new AiConnectivityResult(true, "连接成功", "2026-10-31T00:00:00", "glm-4-flash"));

        mockMvc.perform(post("/admin/settings/ai/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(candidate())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.model").value("glm-4-flash"));

        verify(service).testCandidateConnectivity(Mockito.any(AiModelCandidateDTO.class));
    }

    @Test
    void saveAndActivateEndpointReturnsCurrentSnapshot() throws Exception {
        when(service.saveAndActivate(Mockito.any(AiModelCandidateDTO.class), Mockito.any()))
            .thenReturn(AiRuntimeConfigSnapshot.builder()
                .provider("zhipu").model("glm-4-flash")
                .supervisorModel("glm-4-flash").visionModel("glm-4.6v-flash")
                .timeoutMs(30000).temperature(0.7).version(2L).build());

        mockMvc.perform(post("/admin/settings/ai/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(candidate())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.version").value(2))
            .andExpect(jsonPath("$.data.model").value("glm-4-flash"));

        verify(service).saveAndActivate(Mockito.any(AiModelCandidateDTO.class), Mockito.any());
    }
}
