package com.xx.jaseatschoicejava.agent.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.impl.AiProviderRegistry;
import com.xx.jaseatschoicejava.service.impl.OpenAiCompatibleModelClientFactory;
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
        AiProviderRegistry providerRegistry = new AiProviderRegistry(List.of(
            new ZhipuAiModelClientFactory(aiConfig, zhipuConfig, null),
            new OpenAiCompatibleModelClientFactory(aiConfig, null)));
        ZhipuChatModelFactory factory = new ZhipuChatModelFactory();
        AiCapabilitiesDTO capabilities = factory.chatModelProvider(
            registry, providerRegistry, aiConfig).getCapabilities();

        assertFalse(capabilities.isAvailable());
        assertFalse(capabilities.isTextChat());
        assertFalse(capabilities.isStreaming());
        assertFalse(capabilities.isVision());
    }

    @Test
    void getCapabilities_resolvesActiveOpenAiCompatibleProvider() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("test-key");
        AiModelConfig config = AiModelConfig.builder()
            .provider("openai-compatible").model("glm-5.3-solo")
            .supervisorModel("glm-5.3-solo").visionModel("glm-5.3-solo")
            .baseUrl("http://127.0.0.1:3065/v1").timeoutMs(30000).temperature(0.2).build();

        DynamicAiModelRegistry registry = new DynamicAiModelRegistry();
        registry.activate(config, new NoopChatModel(), new NoopChatModel(),
            new NoopChatModel(), new NoopStreamingChatModel(), 1L);
        AiProviderRegistry providerRegistry = new AiProviderRegistry(List.of(
            new OpenAiCompatibleModelClientFactory(aiConfig, null)));
        ZhipuChatModelFactory factory = new ZhipuChatModelFactory();

        AiCapabilitiesDTO capabilities = factory.chatModelProvider(
            registry, providerRegistry, aiConfig).getCapabilities();

        assertEquals("openai-compatible", capabilities.getProvider());
        assertTrue(capabilities.isAvailable());
        assertTrue(capabilities.isTextChat());
        assertTrue(capabilities.isStreaming());
    }
}
