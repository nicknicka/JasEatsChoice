package com.xx.jaseatschoicejava.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xx.jaseatschoicejava.config.AiEmbeddingRuntimeKeyStore;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AnnouncementRagStartupTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(AnnouncementRagConfig.class, ZhipuAnnouncementEmbeddingModelFactory.class,
            OpenAiCompatibleAnnouncementEmbeddingModelFactory.class, AiEmbeddingRuntimeKeyStore.class);

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
    void embeddingRuntimeKeyConfiguresEmbedding(@TempDir Path tempDir) {
        AiEmbeddingRuntimeKeyStore embeddingKeyStore = new AiEmbeddingRuntimeKeyStore(
            tempDir.resolve("embedding-active-key").toString());
        embeddingKeyStore.writeKey("embedding-runtime-key");

        new ApplicationContextRunner()
            .withUserConfiguration(AnnouncementRagConfig.class, ZhipuAnnouncementEmbeddingModelFactory.class,
                OpenAiCompatibleAnnouncementEmbeddingModelFactory.class)
            .withBean(AiEmbeddingRuntimeKeyStore.class, () -> embeddingKeyStore)
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=",
                "announcement-rag.qdrant.host=localhost",
                "announcement-rag.qdrant.port=1")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.UNAVAILABLE);
            });
    }

    @Test
    void applicationYmlBindsAnnouncementRagQdrantFromEnvironmentStyleKeys() throws Exception {
        var loader = new org.springframework.boot.env.YamlPropertySourceLoader();
        var sources = loader.load(
            "application", new org.springframework.core.io.ClassPathResource("application.yml"));
        var environment = new org.springframework.mock.env.MockEnvironment();
        environment.getPropertySources().addLast(sources.get(0));
        environment.setProperty("ANNOUNCEMENT_RAG_ENABLED", "false");
        environment.setProperty("QDRANT_HOST", "qdrant");
        environment.setProperty("QDRANT_PORT", "6334");
        environment.setProperty("QDRANT_COLLECTION", "test_collection");

        AnnouncementRagProperties properties = org.springframework.boot.context.properties.bind.Binder.get(environment)
            .bind("announcement-rag",
                org.springframework.boot.context.properties.bind.Bindable.of(AnnouncementRagProperties.class))
            .get();

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getQdrant().getHost()).isEqualTo("qdrant");
        assertThat(properties.getQdrant().getPort()).isEqualTo(6334);
        assertThat(properties.getQdrant().getCollection()).isEqualTo("test_collection");
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
