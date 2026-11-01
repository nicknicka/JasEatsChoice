package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

class AnnouncementRagConfigRuntimeKeyTest {

    @TempDir
    Path tempDir;

    @Test
    void runtimeKeyIsUsedBeforeEnvironmentKeys() throws IOException {
        Path keyFile = tempDir.resolve("ai-active-key");
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(keyFile.toString());
        keyStore.writeKey("runtime-embedding-key");

        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("zhipu-key");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");

        EmbeddingModel model = new AnnouncementRagConfig().announcementEmbeddingModel(
            properties, zhipuConfig, aiConfig, keyStore, new DynamicAiModelRegistry(),
            java.util.List.of(new ZhipuAnnouncementEmbeddingModelFactory(),
                new OpenAiCompatibleAnnouncementEmbeddingModelFactory()));

        assertInstanceOf(AnnouncementRagRuntimeEmbeddingModel.class, model);
    }

    @Test
    void deletedRuntimeKeyFallsBackToEnvironmentKeys() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(
            tempDir.resolve("missing").resolve("ai-active-key").toString());

        EmbeddingModel model = new AnnouncementRagConfig().announcementEmbeddingModel(
            properties, zhipuConfig, aiConfig, keyStore, new DynamicAiModelRegistry(),
            java.util.List.of(new ZhipuAnnouncementEmbeddingModelFactory(),
                new OpenAiCompatibleAnnouncementEmbeddingModelFactory()));

        assertInstanceOf(AnnouncementRagRuntimeEmbeddingModel.class, model);
    }


    @Test
    void eachEmbeddingCallResolvesCurrentRuntimeModel() {
        EmbeddingModel firstModel = org.mockito.Mockito.mock(EmbeddingModel.class);
        EmbeddingModel secondModel = org.mockito.Mockito.mock(EmbeddingModel.class);
        java.util.concurrent.atomic.AtomicInteger creations = new java.util.concurrent.atomic.AtomicInteger();
        AnnouncementRagRuntimeEmbeddingModel model = new AnnouncementRagRuntimeEmbeddingModel(() -> {
            if (creations.incrementAndGet() == 1) {
                return firstModel;
            }
            return secondModel;
        });

        model.embedAll(java.util.List.of(dev.langchain4j.data.segment.TextSegment.from("first")));
        model.embedAll(java.util.List.of(dev.langchain4j.data.segment.TextSegment.from("second")));

        assertEquals(2, creations.get());
        org.mockito.Mockito.verify(firstModel)
            .embedAll(java.util.List.of(dev.langchain4j.data.segment.TextSegment.from("first")));
        org.mockito.Mockito.verify(secondModel)
            .embedAll(java.util.List.of(dev.langchain4j.data.segment.TextSegment.from("second")));
    }

    @Test
    void runtimeKeyUsedWhenActiveProviderMatchesEmbeddingProvider() throws IOException {
        Path keyFile = tempDir.resolve("matching-active-key");
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(keyFile.toString());
        keyStore.writeKey("runtime-key");

        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("zhipu-env-key");

        assertEquals("runtime-key", new AnnouncementRagConfig().embeddingApiKey(
            properties, zhipuConfig, new AIConfig(), keyStore, "zhipu"));
    }

    @Test
    void runtimeKeyIgnoredWhenActiveProviderDiffersFromEmbeddingProvider() throws IOException {
        Path keyFile = tempDir.resolve("foreign-active-key");
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(keyFile.toString());
        keyStore.writeKey("third-party-chat-key");

        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("zhipu-env-key");

        assertEquals("zhipu-env-key", new AnnouncementRagConfig().embeddingApiKey(
            properties, zhipuConfig, new AIConfig(), keyStore, "openai-compatible"));
    }

    @Test
    void foreignRuntimeKeyDoesNotEnableEmbeddingWithoutOwnKey() throws IOException {
        Path keyFile = tempDir.resolve("foreign-only-key");
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(keyFile.toString());
        keyStore.writeKey("third-party-chat-key");

        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");

        EmbeddingModel model = new AnnouncementRagConfig().configuredEmbeddingModel(
            properties, zhipuConfig, new AIConfig(), keyStore, "openai-compatible",
            new ZhipuAnnouncementEmbeddingModelFactory());

        assertInstanceOf(AnnouncementRagDisabledEmbeddingModel.class, model);
    }

    @Test
    void activeProviderIsReadFromRegistrySnapshot() {
        DynamicAiModelRegistry registry = new DynamicAiModelRegistry();
        registry.activate(AiModelConfig.builder().provider("openai-compatible").build(),
            null, null, null, 1L);

        assertEquals("openai-compatible", registry.current().config().getProvider());
    }
}
