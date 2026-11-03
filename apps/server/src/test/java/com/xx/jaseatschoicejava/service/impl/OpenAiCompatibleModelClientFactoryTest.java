package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;
import com.xx.jaseatschoicejava.agent.config.NoopChatModel;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelOption;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

class OpenAiCompatibleModelClientFactoryTest {

    private static final String TEST_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";

    @Test
    void environmentConfig_usesGenericConfig() {
        AIConfig aiConfig = new AIConfig();
        aiConfig.setModel("gpt-4o-mini");
        aiConfig.setSupervisorModel("gpt-4o-mini");
        aiConfig.setVisionModel("gpt-4o-mini");
        aiConfig.setBaseUrl("http://127.0.0.1:3065/v1");
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
            aiConfig, mock(AiRuntimeKeyStore.class));

        AiModelConfig config = factory.environmentConfig(aiConfig);

        assertEquals("openai-compatible", config.getProvider());
        assertEquals("gpt-4o-mini", config.getModel());
        assertEquals("http://127.0.0.1:3065/v1", config.getBaseUrl());
        assertFalse(config.isVisionVerified());
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
            .model("gpt-4o-mini")
            .supervisorModel("gpt-4o-mini")
            .visionModel("gpt-4o-mini")
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
            .model("gpt-4o-mini")
            .baseUrl("")
            .timeoutMs(30000)
            .temperature(0.2)
            .build();

        assertInstanceOf(NoopChatModel.class, factory.buildChatModel(config, "test-key"));
    }

    @Test
    void capabilities_visionIsFalseUntilVerifiedByRealImageRequest() {
        AiRuntimeKeyStore keyStore = mock(AiRuntimeKeyStore.class);
        when(keyStore.readKey()).thenReturn("test-key");
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
            new AIConfig(), keyStore);
        AiModelConfig config = AiModelConfig.builder()
            .provider("openai-compatible")
            .model("gpt-4o-mini")
            .visionModel("gpt-4o-mini")
            .baseUrl("http://127.0.0.1:3065/v1")
            .visionVerified(false)
            .build();

        AiCapabilitiesDTO capabilities = factory.capabilities(config);

        assertTrue(capabilities.isAvailable());
        assertFalse(capabilities.isVision());
    }

    @Test
    void capabilities_visionIsTrueWhenVerificationWasPersisted() {
        AiRuntimeKeyStore keyStore = mock(AiRuntimeKeyStore.class);
        when(keyStore.readKey()).thenReturn("test-key");
        OpenAiCompatibleModelClientFactory factory = new OpenAiCompatibleModelClientFactory(
            new AIConfig(), keyStore);
        AiModelConfig config = AiModelConfig.builder()
            .provider("openai-compatible")
            .model("gpt-4o-mini")
            .visionModel("gpt-4o-mini")
            .baseUrl("http://127.0.0.1:3065/v1")
            .visionVerified(true)
            .build();

        AiCapabilitiesDTO capabilities = factory.capabilities(config);

        assertTrue(capabilities.isAvailable());
        assertTrue(capabilities.isVision());
    }

    @Test
    void visionModel_sendsImageContentToOpenAiCompatibleEndpoint() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = "{\"choices\":[{\"message\":{\"content\":\"OK\"}}]}"
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
            AiModelConfig config = AiModelConfig.builder()
                .provider("openai-compatible")
                .model("gpt-4o-mini")
                .supervisorModel("gpt-4o-mini")
                .visionModel("gpt-4o-mini")
                .baseUrl("http://127.0.0.1:" + port + "/v1")
                .timeoutMs(5000)
                .temperature(0.2)
                .visionVerified(true)
                .build();
            ChatModel visionModel = factory.buildVisionModel(config, "test-key");

            String response = visionModel.chat(UserMessage.from(
                dev.langchain4j.data.message.TextContent.from("请只回复 OK，确认你能接收这张图片。"),
                ImageContent.from(TEST_IMAGE))).aiMessage().text();

            assertEquals("OK", response);
            assertTrue(requestBody.get().contains("\"image_url\""));
            assertTrue(requestBody.get().contains(TEST_IMAGE));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void discoverModels_parsesGatewayModelList() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/models", exchange -> {
            byte[] body = ("{\"object\":\"list\",\"data\":["
                + "{\"id\":\"gpt-4o-mini\",\"name\":\"GPT-4o mini\",\"supports_images\":false},"
                + "{\"id\":\"gpt-4o\",\"name\":\"GPT-4o\",\"supports_images\":true}]}")
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
            assertEquals("gpt-4o-mini", options.get(0).getId());
            assertEquals("GPT-4o mini", options.get(0).getName());
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

    @Test
    void recipeModel_usesDedicatedTemperatureAndMaxTokens() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = "{\"choices\":[{\"message\":{\"content\":\"OK\"}}]}"
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
            AiModelConfig config = AiModelConfig.builder()
                .provider("openai-compatible")
                .model("gpt-4o-mini")
                .supervisorModel("gpt-4o-mini")
                .visionModel("gpt-4o-mini")
                .baseUrl("http://127.0.0.1:" + port + "/v1")
                .timeoutMs(5000)
                .temperature(0.9)
                .build();

            ChatModel recipeModel = factory.buildRecipeModel(config, "test-key");
            assertEquals("OK", recipeModel.chat("给一个减脂食谱"));
            String recipeBody = requestBody.get().replaceAll("\\s+", "");
            assertTrue(recipeBody.contains("\"temperature\":0.2"), requestBody.get());
            assertTrue(recipeBody.contains("\"max_tokens\":4096"), requestBody.get());

            ChatModel chatModel = factory.buildChatModel(config, "test-key");
            assertEquals("OK", chatModel.chat("你好"));
            String chatBody = requestBody.get().replaceAll("\\s+", "");
            assertTrue(chatBody.contains("\"temperature\":0.9"), requestBody.get());
            assertTrue(chatBody.contains("\"max_tokens\":4096"), requestBody.get());
        } finally {
            server.stop(0);
        }
    }
}
