package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;
import com.xx.jaseatschoicejava.agent.config.NoopChatModel;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelOption;
import dev.langchain4j.model.openai.OpenAiChatModel;

class OpenAiCompatibleModelClientFactoryTest {

    @Test
    void environmentConfig_usesGenericConfig() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setModel("glm-5.3-solo");
        aiConfig.setSupervisorModel("glm-5.3-solo");
        aiConfig.setVisionModel("glm-5.3-solo");
        aiConfig.setBaseUrl("http://127.0.0.1:3065/v1");
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
            aiConfig, mock(AiRuntimeKeyStore.class));

        AiModelConfig config = factory.environmentConfig(aiConfig);

        assertEquals("openai-compatible", config.getProvider());
        assertEquals("glm-5.3-solo", config.getModel());
        assertEquals("http://127.0.0.1:3065/v1", config.getBaseUrl());
    }

    @Test
    void runtimeKey_winsOverGenericKey() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("generic-key");
        AiRuntimeKeyStore keyStore = mock(AiRuntimeKeyStore.class);
        when(keyStore.readKey()).thenReturn("runtime-key");
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(aiConfig, keyStore);

        assertEquals("runtime-key", factory.resolveKey(keyStore));
    }

    @Test
    void buildChatModel_withBaseUrl_returnsOpenAiModel() {
        AIConfig aiConfig = new AIConfig();
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
            aiConfig, mock(AiRuntimeKeyStore.class));
        AiModelConfig config = AiModelConfig.builder()
            .provider("openai-compatible")
            .model("glm-5.3-solo")
            .supervisorModel("glm-5.3-solo")
            .visionModel("glm-5.3-solo")
            .baseUrl("http://127.0.0.1:3065/v1")
            .timeoutMs(30000)
            .temperature(0.2)
            .build();

        assertInstanceOf(OpenAiChatModel.class, factory.buildChatModel(config, "test-key"));
    }

    @Test
    void buildChatModel_withoutBaseUrl_returnsNoop() {
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
            new AIConfig(), mock(AiRuntimeKeyStore.class));
        AiModelConfig config = AiModelConfig.builder()
            .provider("openai-compatible")
            .model("glm-5.3-solo")
            .baseUrl("")
            .timeoutMs(30000)
            .temperature(0.2)
            .build();

        assertInstanceOf(NoopChatModel.class, factory.buildChatModel(config, "test-key"));
    }

    @Test
    void discoverModels_parsesGatewayModelList() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/models", exchange -> {
            byte[] body = ("{\"object\":\"list\",\"data\":["
                + "{\"id\":\"glm-5.3-solo\",\"name\":\"GLM-5.3\",\"supports_images\":false},"
                + "{\"id\":\"qwen3.8-max-solo\",\"name\":\"Qwen3.8-Max\",\"supports_images\":true}]}")
                .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
                new AIConfig(), mock(AiRuntimeKeyStore.class));
            AiModelCandidateDTO candidate = AiModelCandidateDTO.builder()
                .provider("openai-compatible")
                .baseUrl("http://127.0.0.1:" + port + "/v1/")
                .apiKey("test-key")
                .timeoutMs(5000)
                .build();

            List<AiModelOption> options = factory.discoverModels(candidate);

            assertEquals(2, options.size());
            assertEquals("glm-5.3-solo", options.get(0).getId());
            assertEquals("GLM-5.3", options.get(0).getName());
            assertEquals(Boolean.FALSE, options.get(0).getSupportsImages());
            assertEquals(Boolean.TRUE, options.get(1).getSupportsImages());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void discoverModels_withoutBaseUrlOrKey_returnsEmpty() {
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
            new AIConfig(), mock(AiRuntimeKeyStore.class));

        List<AiModelOption> options = factory.discoverModels(AiModelCandidateDTO.builder()
            .provider("openai-compatible")
            .baseUrl("")
            .apiKey("")
            .build());

        assertEquals(List.of(), options);
    }
}
