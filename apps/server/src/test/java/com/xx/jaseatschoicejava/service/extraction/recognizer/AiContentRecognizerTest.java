package com.xx.jaseatschoicejava.service.extraction.recognizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.enums.ContentType;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

@ExtendWith(MockitoExtension.class)
class AiContentRecognizerTest {

    @Mock
    private ChatModel visionModel;

    @Mock
    private ChatModel aiModel;

    private AiContentRecognizer recognizer;

    @BeforeEach
    void setUp() {
        recognizer = new AiContentRecognizer();
        ReflectionTestUtils.setField(recognizer, "visionModel", visionModel);
        ReflectionTestUtils.setField(recognizer, "aiModel", aiModel);
    }

    @Test
    void videoDoesNotCallVendorApiAndUsesCoverImageWithDegradationReason() {
        AtomicReference<UserMessage> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set(invocation.getArgument(0));
            return ChatResponse.builder().aiMessage(AiMessage.from("{\"isRecipe\":true}")).build();
        }).when(visionModel).chat(any(ChatMessage.class));

        FetchedContent content = FetchedContent.builder()
            .fetchSuccess(true)
            .contentType(ContentType.VIDEO)
            .coverImage("https://example.com/cover.jpg")
            .textContent("视频文字内容")
            .build();

        ContentRecognitionResult result = recognizer.recognize(content);

        assertEquals("{\"isRecipe\":true}", result.response());
        assertEquals("当前未开放视频理解，已使用视频封面图提取", result.degradationReason());
        UserMessage message = captured.get();
        assertTrue(message.contents().stream().anyMatch(ImageContent.class::isInstance));
        verify(aiModel, never()).chat(anyString());
    }

    @Test
    void coverImageFailureFallsBackToTextWithDegradationReason() {
        when(visionModel.chat(any(ChatMessage.class))).thenThrow(new RuntimeException("视觉模型不可用"));
        when(aiModel.chat(anyString())).thenReturn("{\"isRecipe\":true}");

        FetchedContent content = FetchedContent.builder()
            .fetchSuccess(true)
            .contentType(ContentType.VIDEO)
            .coverImage("https://example.com/cover.jpg")
            .textContent("视频文字内容")
            .build();

        ContentRecognitionResult result = recognizer.recognize(content);

        assertEquals("{\"isRecipe\":true}", result.response());
        assertEquals("视频封面图识别失败，已改用视频文字信息提取", result.degradationReason());
        verify(visionModel).chat(any(ChatMessage.class));
        verify(aiModel).chat(anyString());
    }

    @Test
    void imageDataUriIsPassedThroughWithoutDoublePrefix() {
        AtomicReference<UserMessage> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set(invocation.getArgument(0));
            return ChatResponse.builder().aiMessage(AiMessage.from("ok")).build();
        }).when(visionModel).chat(any(ChatMessage.class));

        String dataUri = "data:image/png;base64,UE5H";
        FetchedContent content = FetchedContent.builder()
            .fetchSuccess(true)
            .imageBase64List(List.of(dataUri))
            .textContent("recipe")
            .build();

        ContentRecognitionResult result = recognizer.recognize(content);

        assertEquals("ok", result.response());
        assertNull(result.degradationReason());
        ImageContent image = (ImageContent) captured.get().contents().stream()
            .filter(ImageContent.class::isInstance)
            .findFirst()
            .orElseThrow();
        assertEquals(dataUri, image.image().url().toString());
        assertNull(image.image().base64Data());
    }
}
