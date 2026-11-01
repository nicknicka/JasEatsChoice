package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

class ZhipuAnnouncementEmbeddingModelFactoryTest {

    private final ZhipuAnnouncementEmbeddingModelFactory factory = new ZhipuAnnouncementEmbeddingModelFactory();

    @Test
    void providerCode_isZhipu() {
        assertEquals("zhipu", factory.providerCode());
    }

    @Test
    void build_withApiKey_returnsZhipuEmbeddingModel() {
        EmbeddingModel model = factory.build("test-key", "embedding-3", 2048, "", Duration.ofSeconds(30));

        assertInstanceOf(ZhipuAiEmbeddingModel.class, model);
    }

    @Test
    void build_withBaseUrl_overridesDefaultEndpoint() {
        EmbeddingModel model = factory.build(
            "test-key", "embedding-3", 1024, "http://127.0.0.1:9999/v1", Duration.ofSeconds(5));

        assertInstanceOf(ZhipuAiEmbeddingModel.class, model);
    }
}
