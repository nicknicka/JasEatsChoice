package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

class OpenAiCompatibleAnnouncementEmbeddingModelFactoryTest {

    private final OpenAiCompatibleAnnouncementEmbeddingModelFactory factory =
        new OpenAiCompatibleAnnouncementEmbeddingModelFactory();

    @Test
    void providerCode_isOpenAiCompatible() {
        assertEquals("openai-compatible", factory.providerCode());
    }

    @Test
    void build_withBaseUrl_returnsOpenAiEmbeddingModel() {
        EmbeddingModel model = factory.build(
            "test-key", "bge-m3", 1024, "http://127.0.0.1:11434/v1", Duration.ofSeconds(30));

        assertInstanceOf(OpenAiEmbeddingModel.class, model);
    }

    @Test
    void build_withoutBaseUrl_failsWithActionableMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            factory.build("test-key", "bge-m3", 1024, "", Duration.ofSeconds(30)));

        assertTrue(ex.getMessage().contains("AI_EMBEDDING_BASE_URL"));
    }

    @Test
    void build_withNonPositiveDimensions_skipsDimensionOverride() {
        EmbeddingModel model = factory.build(
            "test-key", "bge-m3", 0, "http://127.0.0.1:11434/v1", Duration.ofSeconds(30));

        assertInstanceOf(OpenAiEmbeddingModel.class, model);
    }
}
