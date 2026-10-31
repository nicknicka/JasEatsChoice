package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;

class AiProviderRegistryTest {

    @Test
    void registry_exposesOpenAiCompatibleAlongsideZhipu() {
        AiRuntimeKeyStore keyStore = mock(AiRuntimeKeyStore.class);
        AiModelClientFactory zhipu = new ZhipuAiModelClientFactory(new AIConfig(), new ZhipuAIConfig(), keyStore);
        AiModelClientFactory openAiCompatible = new OpenAiCompatibleModelClientFactory(new AIConfig(), keyStore);
        AiProviderRegistry registry = new AiProviderRegistry(List.of(zhipu, openAiCompatible));

        Set<String> codes = registry.options().stream()
            .map(AiRuntimeConfigSnapshot.AiProviderOption::getCode)
            .collect(Collectors.toSet());

        assertTrue(codes.contains("zhipu"), "缺少 zhipu provider");
        assertTrue(codes.contains("openai-compatible"), "缺少 openai-compatible provider");
    }

    @Test
    void registry_resolvesOpenAiCompatibleProviderMetadata() {
        AiRuntimeKeyStore keyStore = mock(AiRuntimeKeyStore.class);
        AiProviderRegistry registry = new AiProviderRegistry(List.of(
            new OpenAiCompatibleModelClientFactory(new AIConfig(), keyStore)));

        AiModelClientFactory factory = registry.required("openai-compatible");

        assertEquals("OpenAI 兼容", factory.providerDisplayName());
        assertTrue(factory.knownModels().contains("glm-5.3-solo"));
    }
}
