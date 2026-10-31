package com.xx.jaseatschoicejava.agent.config;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.impl.ZhipuAiModelClientFactory;

class ZhipuChatModelFactoryTest {

    @Test
    void getCapabilities_withoutKeyAndVisionModel_reportsAllCapabilitiesUnavailable() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        zhipuConfig.setVisionModel("");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("");
        aiConfig.setVisionModel("");
        AiModelConfig config = AiModelConfig.builder()
            .provider("zhipu").model("glm-4-flash")
            .supervisorModel("glm-4-flash").visionModel("")
            .baseUrl("").timeoutMs(30000).temperature(0.7).build();

        DynamicAiModelRegistry registry = new DynamicAiModelRegistry();
        registry.activate(config, new NoopChatModel(), new NoopChatModel(),
            new NoopChatModel(), new NoopStreamingChatModel(), 1L);
        ZhipuChatModelFactory factory = new ZhipuChatModelFactory();
        AiCapabilitiesDTO capabilities = factory.chatModelProvider(
            registry,
            new ZhipuAiModelClientFactory(aiConfig, zhipuConfig),
            aiConfig,
            zhipuConfig
        ).getCapabilities();

        assertFalse(capabilities.isAvailable());
        assertFalse(capabilities.isTextChat());
        assertFalse(capabilities.isStreaming());
        assertFalse(capabilities.isVision());
    }
}
