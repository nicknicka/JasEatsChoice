package com.xx.jaseatschoicejava.agent.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.impl.ZhipuAiModelClientFactory;

class ZhipuRuntimeModelKeyTest {

    @TempDir
    Path tempDir;

    @Test
    void runtimeKeyDrivesChatSupervisorVisionAndStreamingAvailability() throws IOException {
        Path keyFile = tempDir.resolve("ai-active-key");
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(keyFile.toString());
        keyStore.writeKey("runtime-model-key");
        ZhipuAiModelClientFactory factory = new ZhipuAiModelClientFactory(
            aiConfig(), zhipuConfig(), keyStore);
        AiModelConfig config = modelConfig();

        assertTrue(factory.capabilities(config).isAvailable());
        assertTrue(factory.capabilities(config).isTextChat());
        assertTrue(factory.capabilities(config).isVision());
        assertFalse(factory.buildChatModel(config) instanceof NoopChatModel);
        assertFalse(factory.buildSupervisorModel(config) instanceof NoopChatModel);
        assertFalse(factory.buildVisionModel(config) instanceof NoopChatModel);
        assertFalse(factory.buildStreamingModel(config) instanceof NoopStreamingChatModel);
    }

    @Test
    void deletedRuntimeKeyFallsBackToGenericEnvironmentKey() {
        ZhipuAiModelClientFactory factory = new ZhipuAiModelClientFactory(
            aiConfig(), zhipuConfig(),
            new AiRuntimeKeyStore(tempDir.resolve("missing").resolve("ai-active-key").toString()));
        AiModelConfig config = modelConfig();

        assertTrue(factory.capabilities(config).isAvailable());
        assertFalse(factory.buildChatModel(config) instanceof NoopChatModel);
    }

    private AIConfig aiConfig() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");
        return aiConfig;
    }

    private ZhipuAIConfig zhipuConfig() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        return zhipuConfig;
    }

    private AiModelConfig modelConfig() {
        return AiModelConfig.builder()
            .provider("zhipu").model("glm-4-flash")
            .supervisorModel("glm-4-flash").visionModel("glm-4.6v-flash")
            .baseUrl("").timeoutMs(30000).temperature(0.7).build();
    }
}
