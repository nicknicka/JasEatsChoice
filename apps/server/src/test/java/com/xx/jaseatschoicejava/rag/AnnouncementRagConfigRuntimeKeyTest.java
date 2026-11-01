package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xx.jaseatschoicejava.config.AIConfig;
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
            properties, zhipuConfig, aiConfig, keyStore, new ZhipuAnnouncementEmbeddingModelFactory());

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
            properties, zhipuConfig, aiConfig, keyStore, new ZhipuAnnouncementEmbeddingModelFactory());

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
}