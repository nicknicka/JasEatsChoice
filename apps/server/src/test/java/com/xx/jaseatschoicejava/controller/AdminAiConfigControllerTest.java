package com.xx.jaseatschoicejava.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiPreviousConfigSummary;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;

class AdminAiConfigControllerTest {

    private AiRuntimeConfigService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(AiRuntimeConfigService.class);
        AdminAiConfigController controller = new AdminAiConfigController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsSnapshotWithoutApiKeyPlaintext() throws Exception {
        AiRuntimeConfigSnapshot snapshot = AiRuntimeConfigSnapshot.builder()
            .provider("zhipu")
            .model("glm-4-flash")
            .supervisorModel("glm-4-flash")
            .visionModel("glm-4.6v-flash")
            .baseUrl("https://ai.example.com")
            .timeoutMs(30000)
            .temperature(0.7)
            .updatedAt("2026-10-31T00:00:00")
            .supportedModels(List.of("glm-4-flash"))
            .providers(List.of(new AiRuntimeConfigSnapshot.AiProviderOption("zhipu", "智谱",
                List.of("glm-4-flash", "glm-4.6v-flash"))))
            .build();
        snapshot.setKeyStatus(new AiRuntimeConfigSnapshot.AiKeyStatus(true, "abcd12345678"));
        when(service.getCurrentSnapshot()).thenReturn(snapshot);

        String body = mockMvc.perform(get("/admin/settings/ai/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.provider").value("zhipu"))
            .andExpect(jsonPath("$.data.model").value("glm-4-flash"))
            .andExpect(jsonPath("$.data.supervisorModel").value("glm-4-flash"))
            .andExpect(jsonPath("$.data.visionModel").value("glm-4.6v-flash"))
            .andExpect(jsonPath("$.data.keyStatus.configured").value(true))
            .andExpect(jsonPath("$.data.keyStatus.last4").doesNotExist())
            .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("secret-key"));
    }

    @Test
    void returnsPreviousConfigSummary() throws Exception {
        when(service.getPreviousConfigSummary()).thenReturn(AiPreviousConfigSummary.builder()
            .version(1L).updatedAt("2026-10-31T00:00:00")
            .provider("zhipu").model("glm-4-flash").supervisorModel("glm-4-flash")
            .visionModel("glm-4.6v-flash").baseUrl("").timeoutMs(30000).temperature(0.7)
            .keyConfigured(true).keyFingerprint("abcd12345678")
            .build());

        mockMvc.perform(get("/admin/settings/ai/previous-summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.version").value(1))
            .andExpect(jsonPath("$.data.model").value("glm-4-flash"))
            .andExpect(jsonPath("$.data.keyFingerprint").value("abcd12345678"));
    }

    @Test
    void rollbackEndpointReturnsCurrentSnapshot() throws Exception {
        when(service.rollbackToPrevious(null)).thenReturn(AiRuntimeConfigSnapshot.builder()
            .version(1L).model("glm-4-flash").build());

        mockMvc.perform(post("/admin/settings/ai/rollback"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.version").value(1))
            .andExpect(jsonPath("$.data.model").value("glm-4-flash"));

        verify(service).rollbackToPrevious(null);
    }

    @Test
    void returnsCurrentConfigConnectivityResult() throws Exception {
        when(service.testCurrentConnectivity()).thenReturn(new AiConnectivityResult(
            true, "连接成功", "2026-10-31T00:00:00", "glm-4-flash"));

        mockMvc.perform(post("/admin/settings/ai/connectivity-test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.message").value("连接成功"))
            .andExpect(jsonPath("$.data.model").value("glm-4-flash"));
    }
}
