package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class AiApiKeyResolverTest {

    @Test
    void runtimeKey_winsOverProviderAndGenericKeys() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("zhipu-key");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");
        AiRuntimeKeyStore runtimeKeyStore = mock(AiRuntimeKeyStore.class);
        when(runtimeKeyStore.readKey()).thenReturn("runtime-key");

        assertEquals("runtime-key", AiApiKeyResolver.resolve(zhipuConfig, aiConfig, runtimeKeyStore));
    }

    @Test
    void missingRuntimeKey_fallsBackToEnvironmentKey() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("zhipu-key");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");
        AiRuntimeKeyStore runtimeKeyStore = mock(AiRuntimeKeyStore.class);
        when(runtimeKeyStore.readKey()).thenReturn("");

        assertEquals("zhipu-key", AiApiKeyResolver.resolve(zhipuConfig, aiConfig, runtimeKeyStore));
    }

    @Test
    void providerKey_winsOverGenericKey() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("zhipu-key");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");

        assertEquals("zhipu-key", AiApiKeyResolver.resolve(zhipuConfig, aiConfig));
    }

    @Test
    void genericKey_isUsedWhenProviderKeyIsBlank() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");

        assertEquals("generic-key", AiApiKeyResolver.resolve(zhipuConfig, aiConfig));
    }

    @Test
    void blankKeys_resolveToEmpty() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey(" ");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("");

        assertEquals("", AiApiKeyResolver.resolve(zhipuConfig, aiConfig));
    }
}
