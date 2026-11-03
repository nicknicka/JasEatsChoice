package com.xx.jaseatschoicejava.agent.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.service.impl.AiProviderRegistry;
import com.xx.jaseatschoicejava.service.impl.OpenAiCompatibleModelClientFactory;

class AiChatModelFactoryTest {

    @Test
    void capabilities_withoutKeyAndBaseUrl_areUnavailable() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("");
        aiConfig.setBaseUrl("");
        AiModelConfig config = AiModelConfig.builder()
            .provider("openai-compatible")
            .model("gpt-4o-mini")
            .supervisorModel("gpt-4o-mini")
            .visionModel("")
            .baseUrl("")
            .timeoutMs(30000)
            .temperature(0.7)
            .build();

        DynamicAiModelRegistry registry = new DynamicAiModelRegistry();
        registry.activate(config, new NoopChatModel(), new NoopChatModel(),
            new NoopChatModel(), 1L);
        AiProviderRegistry providerRegistry = new AiProviderRegistry(List.of(
            new OpenAiCompatibleModelClientFactory(aiConfig, null)));
        AiChatModelFactory factory = new AiChatModelFactory();

        AiCapabilitiesDTO capabilities = factory.chatModelProvider(
            registry, providerRegistry, aiConfig).getCapabilities();

        assertFalse(capabilities.isAvailable());
        assertFalse(capabilities.isTextChat());
        assertFalse(capabilities.isStreaming());
        assertFalse(capabilities.isVision());
    }

    @Test
    void capabilities_requireVerifiedVisionModel() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("test-key");
        AiModelConfig unverified = AiModelConfig.builder()
            .provider("openai-compatible")
            .model("gpt-4o-mini")
            .supervisorModel("gpt-4o-mini")
            .visionModel("gpt-4o-mini")
            .baseUrl("https://api.example.com/v1")
            .timeoutMs(30000)
            .temperature(0.2)
            .visionVerified(false)
            .build();
        DynamicAiModelRegistry registry = new DynamicAiModelRegistry();
        registry.activate(unverified, new NoopChatModel(), new NoopChatModel(),
            new NoopChatModel(), 1L);
        AiChatModelFactory factory = new AiChatModelFactory();
        AiProviderRegistry providerRegistry = new AiProviderRegistry(List.of(
            new OpenAiCompatibleModelClientFactory(aiConfig, null)));

        AiCapabilitiesDTO capabilities = factory.chatModelProvider(
            registry, providerRegistry, aiConfig).getCapabilities();

        assertEquals("openai-compatible", capabilities.getProvider());
        assertTrue(capabilities.isAvailable());
        assertTrue(capabilities.isTextChat());
        assertTrue(capabilities.isStreaming());
        assertFalse(capabilities.isVision());

        AiModelConfig verified = unverified.toBuilder().visionVerified(true).build();
        registry.activate(verified, new NoopChatModel(), new NoopChatModel(),
            new NoopChatModel(), 2L);

        assertTrue(factory.chatModelProvider(registry, providerRegistry, aiConfig)
            .getCapabilities().isVision());
    }
}
