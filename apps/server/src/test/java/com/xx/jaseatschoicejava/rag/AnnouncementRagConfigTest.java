package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.model.embedding.EmbeddingModel;

class AnnouncementRagConfigTest {

    private final AnnouncementRagConfig config = new AnnouncementRagConfig();

    private List<AnnouncementEmbeddingModelFactory> allFactories() {
        return List.of(
            new ZhipuAnnouncementEmbeddingModelFactory(),
            new OpenAiCompatibleAnnouncementEmbeddingModelFactory());
    }

    @Test
    void embeddingModel_withoutKey_returnsDisabledModel() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");

        EmbeddingModel model = config.configuredEmbeddingModel(
            properties, zhipuConfig, new AIConfig(), null, null,
            new ZhipuAnnouncementEmbeddingModelFactory());

        assertInstanceOf(AnnouncementRagDisabledEmbeddingModel.class, model);
    }

    @Test
    void embeddingModel_withKey_returnsConfiguredZhipuModel() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("test-key");

        EmbeddingModel model = config.announcementEmbeddingModel(
            properties, zhipuConfig, new AIConfig(), null, new DynamicAiModelRegistry(),
            allFactories());

        assertInstanceOf(AnnouncementRagRuntimeEmbeddingModel.class, model);
    }

    @Test
    void embeddingModel_withGenericAiKey_returnsConfiguredZhipuModel() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-test-key");

        EmbeddingModel model = config.announcementEmbeddingModel(
            properties, zhipuConfig, aiConfig, null, new DynamicAiModelRegistry(),
            allFactories());

        assertInstanceOf(AnnouncementRagRuntimeEmbeddingModel.class, model);
    }

    @Test
    void selectEmbeddingModelFactory_defaultProvider_returnsZhipuFactory() {
        AnnouncementEmbeddingModelFactory factory =
            config.selectEmbeddingModelFactory(allFactories(), "zhipu");

        assertInstanceOf(ZhipuAnnouncementEmbeddingModelFactory.class, factory);
    }

    @Test
    void selectEmbeddingModelFactory_openAiCompatibleProvider_returnsOpenAiFactory() {
        AnnouncementEmbeddingModelFactory factory =
            config.selectEmbeddingModelFactory(allFactories(), "openai-compatible");

        assertInstanceOf(OpenAiCompatibleAnnouncementEmbeddingModelFactory.class, factory);
    }

    @Test
    void selectEmbeddingModelFactory_unknownProvider_throwsWithAvailableCodes() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            config.selectEmbeddingModelFactory(allFactories(), "unknown-provider"));

        assertTrue(ex.getMessage().contains("unknown-provider"));
        assertTrue(ex.getMessage().contains("zhipu"));
        assertTrue(ex.getMessage().contains("openai-compatible"));
    }

    @Test
    void selectEmbeddingModelFactory_noImplementations_throws() {
        assertThrows(IllegalStateException.class, () ->
            config.selectEmbeddingModelFactory(List.of(), "zhipu"));
    }

    @Test
    void announcementEmbeddingModel_unknownProvider_throwsInsteadOfFallingBack() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        properties.getEmbedding().setProvider("unknown-provider");
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("test-key");

        assertThrows(IllegalArgumentException.class, () ->
            config.announcementEmbeddingModel(
                properties, zhipuConfig, new AIConfig(), null, new DynamicAiModelRegistry(),
                allFactories()));
    }
}
