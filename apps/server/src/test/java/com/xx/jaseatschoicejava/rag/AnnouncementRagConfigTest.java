package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

class AnnouncementRagConfigTest {

    private final AnnouncementRagConfig config = new AnnouncementRagConfig();

    @Test
    void embeddingModel_withoutKey_returnsDisabledModel() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");

        EmbeddingModel model = config.announcementEmbeddingModel(properties, zhipuConfig);

        assertInstanceOf(AnnouncementRagDisabledEmbeddingModel.class, model);
    }

    @Test
    void embeddingModel_withKey_returnsConfiguredZhipuModel() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("test-key");

        EmbeddingModel model = config.announcementEmbeddingModel(properties, zhipuConfig);

        assertInstanceOf(ZhipuAiEmbeddingModel.class, model);
    }
}
