package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class AiApiKeyResolverTest {

    @Test
    void runtimeKey_winsOverEnvironmentKey() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("environment-key");
        AiRuntimeKeyStore runtimeKeyStore = mock(AiRuntimeKeyStore.class);
        when(runtimeKeyStore.readKey()).thenReturn("runtime-key");

        assertEquals("runtime-key", AiApiKeyResolver.resolve(aiConfig, runtimeKeyStore));
    }

    @Test
    void missingRuntimeKey_fallsBackToEnvironmentKey() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("environment-key");
        AiRuntimeKeyStore runtimeKeyStore = mock(AiRuntimeKeyStore.class);
        when(runtimeKeyStore.readKey()).thenReturn("");

        assertEquals("environment-key", AiApiKeyResolver.resolve(aiConfig, runtimeKeyStore));
    }

    @Test
    void blankRuntimeKey_fallsBackToEnvironmentKey() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("environment-key");
        AiRuntimeKeyStore runtimeKeyStore = mock(AiRuntimeKeyStore.class);
        when(runtimeKeyStore.readKey()).thenReturn(" ");

        assertEquals("environment-key", AiApiKeyResolver.resolve(aiConfig, runtimeKeyStore));
    }

    @Test
    void blankKeys_resolveToEmpty() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey(" ");

        assertEquals("", AiApiKeyResolver.resolve(aiConfig, ""));
    }

    @Test
    void nullConfigAndStore_resolveToEmpty() {
        assertEquals("", AiApiKeyResolver.resolve(null, (AiRuntimeKeyStore) null));
    }
}
