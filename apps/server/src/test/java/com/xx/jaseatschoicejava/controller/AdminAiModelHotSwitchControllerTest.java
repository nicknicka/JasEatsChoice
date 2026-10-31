package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void currentAndSaveResponsesDoNotEchoKeyMaterial() throws Exception {
        String candidateKey = "controller-candidate-key-0987654321";
        AiModelCandidateDTO candidate = candidate();
        candidate.setApiKey(candidateKey);
        AiRuntimeConfigSnapshot snapshot = AiRuntimeConfigSnapshot.builder()
            .provider("zhipu").model("glm-4-flash")
            .supervisorModel("glm-4-flash").visionModel("glm-4.6v-flash")
            .timeoutMs(30000).temperature(0.7).version(2L)
            .keyStatus(new AiRuntimeConfigSnapshot.AiKeyStatus(true, "8d43aa55aa4f"))
            .build();
        when(service.saveAndActivate(Mockito.any(AiModelCandidateDTO.class), Mockito.any()))
            .thenReturn(snapshot);
        when(service.getCurrentSnapshot()).thenReturn(snapshot);

        String activationResponse = mockMvc.perform(post("/admin/settings/ai/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(candidate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.version").value(2))
            .andExpect(jsonPath("$.data.keyStatus.configured").value(true))
            .andExpect(jsonPath("$.data.keyStatus.fingerprint").value("8d43aa55aa4f"))
            .andReturn().getResponse().getContentAsString();

        String currentResponse = mockMvc.perform(get("/admin/settings/ai/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.keyStatus.configured").value(true))
            .andReturn().getResponse().getContentAsString();

        assertFalse(activationResponse.contains(candidateKey));
        assertFalse(currentResponse.contains(candidateKey));
        verify(service).saveAndActivate(Mockito.any(AiModelCandidateDTO.class), Mockito.any());
        verify(service).getCurrentSnapshot();
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
