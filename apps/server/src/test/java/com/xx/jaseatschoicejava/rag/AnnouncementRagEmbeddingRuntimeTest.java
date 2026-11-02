package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiEmbeddingRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AnnouncementEmbeddingRuntime;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * 公告 Embedding 运行时的解析与校验行为：配置按调用解析、key 槽位独立、维度与向量库比对。
 */
class AnnouncementRagEmbeddingRuntimeTest {

    @TempDir
    Path tempDir;

    @Test
    void environmentKeyConfiguresEmbedding() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-test-key");

        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 2048, ""), zhipuConfig, aiConfig, null,
            List.of(measuringFactory("zhipu", 2048)));

        assertInstanceOf(EmbeddingModel.class, runtime.model());
        assertNotNull(runtime.usable());
    }

    @Test
    void missingKey_returnsDisabledModelAndNoUsableConfig() {
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 2048, ""), new ZhipuAIConfig(), new AIConfig(), null,
            List.of(measuringFactory("zhipu", 2048)));

        assertInstanceOf(AnnouncementRagDisabledEmbeddingModel.class, runtime.model());
        assertNull(runtime.usable());
    }

    @Test
    void keylessOpenAiCompatibleEndpointWithBaseUrlBuildsModel() {
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("openai-compatible", "bge-m3", 1024, "http://localhost:11434/v1"),
            new ZhipuAIConfig(), new AIConfig(), null,
            List.of(measuringFactory("openai-compatible", 1024)));

        assertInstanceOf(EmbeddingModel.class, runtime.model());
        assertNotNull(runtime.usable());
    }

    @Test
    void runtimeKeySlotIsUsedBeforeEnvironmentKeys() throws Exception {
        AiEmbeddingRuntimeKeyStore keyStore = keyStore("embedding-runtime-key");
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("zhipu-env-key");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-env-key");
        AnnouncementEmbeddingModelFactory factory = measuringFactory("zhipu", 2048);

        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 2048, ""), zhipuConfig, aiConfig, keyStore, List.of(factory));

        runtime.model();

        verify(factory).build("embedding-runtime-key", "embedding-3", 2048, "",
            java.time.Duration.ofMillis(30000));
    }

    @Test
    void runtimeConfigIsResolvedOnEachCall() {
        AnnouncementRagProperties properties = properties("zhipu", "embedding-3", 2048, "");
        AnnouncementEmbeddingConfigHolder holder = new AnnouncementEmbeddingConfigHolder(properties);
        AnnouncementEmbeddingModelFactory zhipuFactory = measuringFactory("zhipu", 2048);
        AnnouncementEmbeddingModelFactory openAiFactory = measuringFactory("openai-compatible", 1024);

        AnnouncementRagEmbeddingRuntime runtime = new AnnouncementRagEmbeddingRuntime(
            properties, holder, null, withKey("test-key"), new AIConfig(),
            List.of(zhipuFactory, openAiFactory), null, null);

        runtime.model();
        holder.update(new AnnouncementEmbeddingConfig(
            "openai-compatible", "bge-m3", 1024, "http://localhost:11434/v1"));
        runtime.model();

        verify(zhipuFactory).build("test-key", "embedding-3", 2048, "",
            java.time.Duration.ofMillis(30000));
        verify(openAiFactory).build("test-key", "bge-m3", 1024,
            "http://localhost:11434/v1", java.time.Duration.ofMillis(30000));
    }

    @Test
    void effectiveFillsBlankPersistedFieldsFromEnvironment() {
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 2048, ""), new ZhipuAIConfig(), new AIConfig(), null,
            List.of(measuringFactory("zhipu", 2048)));

        AnnouncementEmbeddingConfig effective = runtime.effective(
            new AnnouncementEmbeddingConfig("", null, 0, null));

        assertEquals("zhipu", effective.provider());
        assertEquals("embedding-3", effective.model());
        assertEquals(2048, effective.dimensions());
        assertEquals("", effective.baseUrl());
    }

    @Test
    void validateRunsRealEmbeddingCallWithMatchingConfiguredDimension() {
        EmbeddingModel model = measuringModel(2048);
        AnnouncementEmbeddingModelFactory factory = factory("zhipu", model);
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 2048, ""), withKey("test-key"), new AIConfig(), null,
            List.of(factory));

        runtime.validate(new AnnouncementEmbeddingConfig("zhipu", "embedding-3", 2048, ""), null);

        verify(model).embed("ping");
    }

    @Test
    void validateRejectsConfiguredDimensionDifferentFromMeasured() {
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 1024, ""), withKey("test-key"), new AIConfig(), null,
            List.of(measuringFactory("zhipu", 2048)));

        BusinessException error = assertThrows(BusinessException.class, () -> runtime.validate(
            new AnnouncementEmbeddingConfig("zhipu", "embedding-3", 1024, ""), null));

        assertEquals("EMBEDDING_DIMENSION_CONFIG_MISMATCH", error.getCode());
    }

    @Test
    void validateRejectsKeylessProviderWithoutBaseUrl() {
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("openai-compatible", "bge-m3", 1024, ""), new ZhipuAIConfig(), new AIConfig(), null,
            List.of(measuringFactory("openai-compatible", 1024)));

        BusinessException error = assertThrows(BusinessException.class, () -> runtime.validate(
            new AnnouncementEmbeddingConfig("openai-compatible", "bge-m3", 1024, ""), null));

        assertEquals("EMBEDDING_CONFIG_INCOMPLETE", error.getCode());
    }

    @Test
    void validateWrapsConnectivityFailure() {
        AnnouncementEmbeddingModelFactory factory = mock(AnnouncementEmbeddingModelFactory.class);
        lenient().when(factory.providerCode()).thenReturn("zhipu");
        when(factory.isConfigured("test-key", "")).thenReturn(true);
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed("ping")).thenThrow(new RuntimeException("连接被拒绝"));
        when(factory.build(any(), any(), anyInt(), any(), any())).thenReturn(model);
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 2048, ""), withKey("test-key"), new AIConfig(), null,
            List.of(factory));

        BusinessException error = assertThrows(BusinessException.class, () -> runtime.validate(
            new AnnouncementEmbeddingConfig("zhipu", "embedding-3", 2048, ""), null));

        assertEquals("EMBEDDING_CONNECTIVITY_FAILED", error.getCode());
        assertTrue(error.getMessage().contains("连接被拒绝"));
    }

    @Test
    void providersAndKeyStatusDescribeRuntimeSlot() throws Exception {
        AnnouncementRagEmbeddingRuntime runtime = runtime(
            properties("zhipu", "embedding-3", 2048, ""), withKey(""), new AIConfig(),
            keyStore("embedding-runtime-key"),
            List.of(measuringFactory("zhipu", 2048), measuringFactory("openai-compatible", 1024)));

        assertEquals(List.of("zhipu", "openai-compatible"), runtime.providers());

        AnnouncementEmbeddingRuntime.KeyStatus status = runtime.keyStatus();
        assertTrue(status.configured());
        assertNotNull(status.fingerprint());
        assertFalse(status.fingerprint().contains("embedding-runtime-key"));
    }

    @Test
    void selectFactoryRejectsUnknownProviderWithAvailableCodes() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
            AnnouncementRagConfig.selectEmbeddingModelFactory(
                List.of(measuringFactory("zhipu", 2048),
                    measuringFactory("openai-compatible", 1024)),
                "unknown-provider"));

        assertTrue(error.getMessage().contains("unknown-provider"));
        assertTrue(error.getMessage().contains("zhipu"));
        assertTrue(error.getMessage().contains("openai-compatible"));
    }

    @Test
    void selectFactoryWithoutImplementations_throws() {
        assertThrows(IllegalStateException.class, () ->
            AnnouncementRagConfig.selectEmbeddingModelFactory(List.of(), "zhipu"));
    }

    private AnnouncementRagEmbeddingRuntime runtime(
            AnnouncementRagProperties properties,
            ZhipuAIConfig zhipuConfig,
            AIConfig aiConfig,
            AiEmbeddingRuntimeKeyStore keyStore,
            List<AnnouncementEmbeddingModelFactory> factories) {
        return new AnnouncementRagEmbeddingRuntime(
            properties, new AnnouncementEmbeddingConfigHolder(properties), keyStore,
            zhipuConfig, aiConfig, factories, null, null);
    }

    private ZhipuAIConfig withKey(String key) {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey(key);
        return zhipuConfig;
    }

    private AiEmbeddingRuntimeKeyStore keyStore(String key) throws Exception {
        AiEmbeddingRuntimeKeyStore store = new AiEmbeddingRuntimeKeyStore(
            tempDir.resolve("embedding-active-key").toString());
        if (key != null) {
            store.writeKey(key);
        }
        return store;
    }

    private AnnouncementRagProperties properties(String provider, String model, int dimensions, String baseUrl) {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        properties.getEmbedding().setProvider(provider);
        properties.getEmbedding().setModel(model);
        properties.getEmbedding().setDimensions(dimensions);
        properties.getEmbedding().setBaseUrl(baseUrl);
        return properties;
    }

    private AnnouncementEmbeddingModelFactory measuringFactory(String provider, int dimensions) {
        return factory(provider, measuringModel(dimensions));
    }

    private EmbeddingModel measuringModel(int dimensions) {
        EmbeddingModel model = mock(EmbeddingModel.class);
        lenient().when(model.embed("ping")).thenReturn(Response.from(Embedding.from(new float[dimensions])));
        return model;
    }

    private AnnouncementEmbeddingModelFactory factory(String provider, EmbeddingModel model) {
        AnnouncementEmbeddingModelFactory factory = mock(AnnouncementEmbeddingModelFactory.class);
        lenient().when(factory.providerCode()).thenReturn(provider);
        lenient().when(factory.isConfigured(any(), any())).thenAnswer(invocation -> {
            String apiKey = invocation.getArgument(0);
            String baseUrl = invocation.getArgument(1);
            return (apiKey != null && !apiKey.isBlank()) || (baseUrl != null && !baseUrl.isBlank());
        });
        lenient().when(factory.build(any(), any(), anyInt(), any(), any())).thenReturn(model);
        return factory;
    }
}
