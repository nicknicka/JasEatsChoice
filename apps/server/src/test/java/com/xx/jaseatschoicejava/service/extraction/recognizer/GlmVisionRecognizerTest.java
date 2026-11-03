package com.xx.jaseatschoicejava.service.extraction.recognizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

@ExtendWith(MockitoExtension.class)
class GlmVisionRecognizerTest {

    @TempDir
    Path tempDir;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ChatModel visionModel;

    private DynamicAiModelRegistry registry;
    private AiRuntimeKeyStore keyStore;
    private GlmVisionRecognizer recognizer;

    @BeforeEach
    void setUp() {
        registry = new DynamicAiModelRegistry();
        keyStore = new AiRuntimeKeyStore(tempDir.resolve("ai-active-key").toString());
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("bootstrap-key-1234567890");
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("bootstrap-key-1234567890");
        zhipuConfig.setBaseUrl("https://bootstrap.example.com");
        zhipuConfig.setVisionModel("bootstrap-vision");
        zhipuConfig.setTimeout(30000);
        recognizer = new GlmVisionRecognizer();
        ReflectionTestUtils.setField(recognizer, "visionModel", visionModel);
        ReflectionTestUtils.setField(recognizer, "aiModel", mock(dev.langchain4j.model.chat.ChatModel.class));
        ReflectionTestUtils.setField(recognizer, "aiConfig", aiConfig);
        ReflectionTestUtils.setField(recognizer, "zhipuAIConfig", zhipuConfig);
        ReflectionTestUtils.setField(recognizer, "runtimeKeyStore", keyStore);
        ReflectionTestUtils.setField(recognizer, "registry", registry);
        ReflectionTestUtils.setField(recognizer, "restTemplate", restTemplate);
    }

    @Test
    void videoRecognitionUsesRuntimeActiveKeyAndModel() {
        keyStore.writeKey("runtime-key-1234567890");
        registry.activate(new DynamicAiModelRegistry.ActiveModels(
            AiModelConfig.builder()
                .provider("zhipu")
                .model("runtime-chat")
                .supervisorModel("runtime-chat")
                .visionModel("runtime-vision")
                .baseUrl("https://runtime.example.com")
                .timeoutMs(30000)
                .temperature(0.7)
                .build(),
            mock(dev.langchain4j.model.chat.ChatModel.class),
            mock(dev.langchain4j.model.chat.ChatModel.class),
            mock(dev.langchain4j.model.chat.ChatModel.class),
            mock(dev.langchain4j.model.chat.StreamingChatModel.class),
            2L));
        when(restTemplate.exchange(eq("https://runtime.example.com"), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Map.class)))
            .thenReturn(ResponseEntity.ok(Map.of("choices", java.util.List.of(
                Map.of("message", Map.of("content", "ok"))))));
        FetchedContent content = FetchedContent.builder()
            .fetchSuccess(true)
            .videoUrl("https://example.com/video.mp4")
            .textContent("recipe")
            .build();

        String result = recognizer.recognize(content);

        assertEquals("ok", result);
        org.mockito.ArgumentCaptor<HttpEntity<Map<String, Object>>> captor =
            org.mockito.ArgumentCaptor.forClass((Class) HttpEntity.class);
        org.mockito.Mockito.verify(restTemplate).exchange(eq("https://runtime.example.com"), eq(HttpMethod.POST),
            captor.capture(), eq(Map.class));
        HttpEntity<Map<String, Object>> entity = captor.getValue();
        assertEquals("Bearer runtime-key-1234567890", entity.getHeaders().getFirst("Authorization"));
        assertEquals("runtime-vision", entity.getBody().get("model"));
    }

    @Test
    void imageDataUriIsPassedThroughWithoutDoublePrefix() {
        AtomicReference<UserMessage> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set((UserMessage) invocation.getArgument(0));
            return ChatResponse.builder().aiMessage(AiMessage.from("ok")).build();
        }).when(visionModel).chat(any(ChatMessage.class));

        String dataUri = "data:image/png;base64,UE5H";
        FetchedContent content = FetchedContent.builder()
            .fetchSuccess(true)
            .imageBase64List(List.of(dataUri))
            .textContent("recipe")
            .build();

        assertEquals("ok", recognizer.recognize(content));

        ImageContent image = (ImageContent) captured.get().contents().stream()
            .filter(c -> c instanceof ImageContent)
            .findFirst()
            .orElseThrow();
        // 抓取阶段已是完整 data URI：直接作为 url 传入，供应商适配层不会再拼一层 data:image/jpeg;base64,
        assertEquals(dataUri, image.image().url().toString());
        assertNull(image.image().base64Data());
    }
}
