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
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;

class AiProviderRegistryTest {

    @Test
    void registry_exposesOnlyOpenAiCompatibleProvider() {
        AiRuntimeKeyStore keyStore = mock(AiRuntimeKeyStore.class);
        AiModelClientFactory openAiCompatible =
            new OpenAiCompatibleModelClientFactory(new AIConfig(), keyStore);
        AiProviderRegistry registry = new AiProviderRegistry(List.of(openAiCompatible));

        Set<String> codes = registry.options().stream()
            .map(AiRuntimeConfigSnapshot.AiProviderOption::getCode)
            .collect(Collectors.toSet());

        assertEquals(Set.of(com.xx.jaseatschoicejava.config.AiProviderCompatibility.PROVIDER_CODE), codes);
    }

    @Test
    void registry_resolvesOpenAiCompatibleProviderMetadata() {
        AiRuntimeKeyStore keyStore = mock(AiRuntimeKeyStore.class);
        AiProviderRegistry registry = new AiProviderRegistry(List.of(
            new OpenAiCompatibleModelClientFactory(new AIConfig(), keyStore)));

        AiModelClientFactory factory = registry.required("openai-compatible");

        assertEquals("OpenAI 兼容", factory.providerDisplayName());
        assertTrue(factory.knownModels().contains("gpt-4o-mini"));
    }
}
