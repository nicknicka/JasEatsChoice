package com.xx.jaseatschoicejava.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AnnouncementRagStartupTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(AnnouncementRagConfig.class, ZhipuAnnouncementEmbeddingModelFactory.class,
            OpenAiCompatibleAnnouncementEmbeddingModelFactory.class)
        .withBean("aiRuntimeKeyStore", AiRuntimeKeyStore.class, () -> new AiRuntimeKeyStore(""))
        .withBean(DynamicAiModelRegistry.class, DynamicAiModelRegistry::new);

    @Test
    void disabledRag_startsInDisabledState() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=false",
                "zhipuai.api-key=test-key")
            .run(context -> {
                assertThat(context).hasSingleBean(AnnouncementRagInfrastructure.class);
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.DISABLED);
            });
    }

    @Test
    void missingEmbeddingKey_startsInNotConfiguredState() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.NOT_CONFIGURED);
            });
    }

    @Test
    void genericAiKey_configuresEmbeddingAndReachesQdrantStage() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=",
                "ai.api-key=generic-test-key",
                "announcement-rag.qdrant.host=localhost",
                "announcement-rag.qdrant.port=1")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.UNAVAILABLE);
            });
    }

    @Test
    void foreignRuntimeKeyDoesNotConfigureEmbedding(@TempDir Path tempDir) throws IOException {
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(tempDir.resolve("active-key").toString());
        keyStore.writeKey("third-party-chat-key");
        DynamicAiModelRegistry registry = new DynamicAiModelRegistry();
        registry.activate(AiModelConfig.builder().provider("openai-compatible").build(),
            null, null, null, 1L);

        new ApplicationContextRunner()
            .withUserConfiguration(AnnouncementRagConfig.class, ZhipuAnnouncementEmbeddingModelFactory.class,
                OpenAiCompatibleAnnouncementEmbeddingModelFactory.class)
            .withBean("aiRuntimeKeyStore", AiRuntimeKeyStore.class, () -> keyStore)
            .withBean(DynamicAiModelRegistry.class, () -> registry)
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.NOT_CONFIGURED);
            });
    }

    @Test
    void keylessOpenAiCompatibleEndpointPassesConfigurationGate() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=",
                "announcement-rag.embedding.provider=openai-compatible",
                "announcement-rag.embedding.model=bge-m3",
                "announcement-rag.embedding.dimensions=1024",
                "announcement-rag.embedding.base-url=http://localhost:11434/v1",
                "announcement-rag.qdrant.host=localhost",
                "announcement-rag.qdrant.port=1")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.UNAVAILABLE);
            });
    }

    @Test
    void unavailableQdrant_startsInUnavailableState() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=test-key",
                "announcement-rag.qdrant.host=localhost",
                "announcement-rag.qdrant.port=1")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.UNAVAILABLE);
            });
    }
}
