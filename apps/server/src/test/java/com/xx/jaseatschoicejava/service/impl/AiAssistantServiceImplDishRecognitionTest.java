package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.service.AiAssistantService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceImplDishRecognitionTest {

    @Mock
    private ChatModel visionModel;

    private AiAssistantService service;

    @BeforeEach
    void setUp() {
        AiAssistantServiceImpl implementation = new AiAssistantServiceImpl();
        ReflectionTestUtils.setField(implementation, "visionModel", visionModel);
        service = implementation;
    }

    @Test
    void recognizeDishWithBase64_returnsFrozenContractAndPassesRealMime() {
        when(visionModel.chat(any(UserMessage.class))).thenReturn(chatResponse("""
            {
              "isDish": true,
              "name": "番茄炒蛋",
              "calories": 180,
              "protein": 10,
              "fat": 12,
              "carbs": 8,
              "difficulty": "简单",
              "preparationTime": "15分钟",
              "ingredients": ["番茄", "鸡蛋"],
              "tags": ["家常菜"],
              "confidence": 0.92,
              "nutritionScore": 8
            }
            """));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/webp");

        assertFalse((Boolean) result.get("error"));
        assertEquals(true, result.get("isDish"));
        assertEquals("番茄炒蛋", result.get("name"));
        assertEquals(180, result.get("calories"));
        assertEquals(10, result.get("protein"));
        assertEquals(12, result.get("fat"));
        assertEquals(8, result.get("carbs"));
        assertEquals("简单", result.get("difficulty"));
        assertEquals("15分钟", result.get("preparationTime"));
        assertEquals(List.of("番茄", "鸡蛋"), result.get("ingredients"));
        assertEquals(List.of("家常菜"), result.get("tags"));
        assertEquals(0.92, result.get("confidence"));
        assertEquals(8, result.get("nutritionScore"));
        assertEquals(true, result.get("nutritionEstimated"));

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(visionModel).chat(messageCaptor.capture());
        UserMessage userMessage = (UserMessage) messageCaptor.getValue();
        ImageContent imageContent = userMessage.contents().stream()
            .filter(ImageContent.class::isInstance)
            .map(ImageContent.class::cast)
            .findFirst()
            .orElseThrow();
        assertEquals("image/webp", imageContent.image().mimeType());
    }

    @Test
    void recognizeDishWithBase64_nonDishReturnsUnderstandableBusinessFailure() {
        when(visionModel.chat(any(UserMessage.class))).thenReturn(chatResponse("""
            {"isDish": false, "reason": "请上传食物图片"}
            """));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/jpeg");

        assertEquals(true, result.get("error"));
        assertEquals(true, result.get("notDish"));
        assertEquals("请上传食物图片", result.get("message"));
    }

    @Test
    void recognizeDishWithBase64_normalizesContractFieldTypes() {
        when(visionModel.chat(any(UserMessage.class))).thenReturn(chatResponse("""
            {
              "isDish": true,
              "name": "青椒肉丝",
              "calories": "220",
              "protein": "15.5",
              "fat": "12",
              "carbs": "10",
              "ingredients": "青椒、猪肉",
              "tags": null,
              "confidence": "0.85",
              "nutritionScore": "7"
            }
            """));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/png");

        assertFalse((Boolean) result.get("error"));
        assertEquals(220.0, result.get("calories"));
        assertEquals(15.5, result.get("protein"));
        assertEquals(List.of("青椒、猪肉"), result.get("ingredients"));
        assertEquals(List.of(), result.get("tags"));
        assertEquals(0.85, result.get("confidence"));
        assertEquals(7.0, result.get("nutritionScore"));
    }

    @Test
    void recognizeDishWithBase64_missingIsDishIsFailure() {
        when(visionModel.chat(any(UserMessage.class))).thenReturn(chatResponse("""
            {"name": "番茄炒蛋", "calories": 180}
            """));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/jpeg");

        assertEquals(true, result.get("error"));
        assertTrue(((String) result.get("message")).contains("缺少菜品判定"));
    }

    @Test
    void recognizeDishWithBase64_invalidModelJsonIsFailureNotDefaultSuccess() {
        when(visionModel.chat(any(UserMessage.class))).thenReturn(chatResponse("不是 JSON"));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/png");

        assertEquals(true, result.get("error"));
        assertTrue(((String) result.get("message")).contains("解析失败"));
        assertFalse(result.containsKey("isDish"));
    }

    @Test
    void recognizeDishWithBase64_timeoutReturnsUnifiedTimeoutMessage() {
        when(visionModel.chat(any(UserMessage.class)))
            .thenThrow(new dev.langchain4j.exception.TimeoutException("read timed out"));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/jpeg");

        assertEquals(true, result.get("error"));
        assertEquals("识别超时，请稍后重试", result.get("message"));
    }

    @Test
    void recognizeDishWithBase64_modelRejectsImageReturnsVisionUnsupported() {
        when(visionModel.chat(any(UserMessage.class)))
            .thenThrow(new dev.langchain4j.exception.InvalidRequestException(
                "This model does not support image input."));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/png");

        assertEquals(true, result.get("error"));
        assertEquals(true, result.get("visionUnsupported"));
        assertEquals("当前视觉模型不支持图片输入，菜品识别暂不可用", result.get("message"));
    }

    @Test
    void recognizeDishWithBase64_http400CauseIsVisionUnsupported() {
        when(visionModel.chat(any(UserMessage.class)))
            .thenThrow(new RuntimeException("upstream failed",
                new dev.langchain4j.exception.HttpException(400, "bad image")));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/png");

        assertEquals(true, result.get("visionUnsupported"));
    }

    @Test
    void recognizeDishWithBase64_serverErrorIsNotTreatedAsVisionUnsupported() {
        when(visionModel.chat(any(UserMessage.class)))
            .thenThrow(new dev.langchain4j.exception.InternalServerException("upstream boom"));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/png");

        assertEquals(true, result.get("error"));
        assertFalse(result.containsKey("visionUnsupported"), "5xx 不能判定为视觉能力不可用");
        assertEquals("菜品识别失败，请稍后重试", result.get("message"));
    }

    @Test
    void recognizeDishWithBase64_rateLimitIsNotTreatedAsVisionUnsupported() {
        when(visionModel.chat(any(UserMessage.class)))
            .thenThrow(new dev.langchain4j.exception.RateLimitException("429 rate limited"));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/png");

        assertEquals(true, result.get("error"));
        assertFalse(result.containsKey("visionUnsupported"), "限流不能判定为视觉能力不可用");
    }

    @Test
    void recognizeDishWithBase64_unsupportedFeatureIsVisionUnsupported() {
        when(visionModel.chat(any(UserMessage.class)))
            .thenThrow(new dev.langchain4j.exception.UnsupportedFeatureException("images not supported"));

        Map<String, Object> result = service.recognizeDishWithBase64("AAAA", "image/png");

        assertEquals(true, result.get("visionUnsupported"));
    }

    private ChatResponse chatResponse(String text) {
        return ChatResponse.builder().aiMessage(AiMessage.from(text)).build();
    }
}
