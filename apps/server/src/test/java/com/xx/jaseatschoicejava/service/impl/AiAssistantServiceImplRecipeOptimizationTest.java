package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.agent.config.NoopChatModel;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.service.AiAssistantService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * 食谱优化任务：目标进入 Prompt、单方案解析、统一结果结构与按版本缓存。
 */
@ExtendWith(MockitoExtension.class)
class AiAssistantServiceImplRecipeOptimizationTest {

    private static final String SINGLE_PLAN_JSON = """
        {
          "goal": "fat_loss",
          "recipe": {
            "name": "低脂番茄炒蛋",
            "difficulty": "简单",
            "ingredients": "鸡蛋2个、番茄2个、油5g",
            "steps": "少油翻炒"
          },
          "nutrition": {
            "original": {"calorie": 240, "protein": 12, "fat": 16, "carb": 8},
            "optimized": {"calorie": 160, "protein": 14, "fat": 6, "carb": 7}
          },
          "improvements": [
            {"title": "减少用油", "detail": "油量由 15g 降至 5g"}
          ],
          "estimationNote": "营养数值为 AI 估算，仅供参考"
        }
        """;

    @Mock
    private DynamicAiModelRegistry dynamicAiModelRegistry;

    @Mock
    private AiProviderRegistry aiProviderRegistry;

    @Mock
    private AiModelClientFactory clientFactory;

    @Mock
    private ChatModel recipeChatModel;

    private AiAssistantServiceImpl implementation;
    private AiAssistantService service;

    @BeforeEach
    void setUp() {
        implementation = new AiAssistantServiceImpl();
        ReflectionTestUtils.setField(implementation, "dynamicAiModelRegistry", dynamicAiModelRegistry);
        ReflectionTestUtils.setField(implementation, "aiProviderRegistry", aiProviderRegistry);
        service = implementation;
    }

    private AiModelConfig config(String provider) {
        return AiModelConfig.builder()
            .provider(provider)
            .model("gpt-4o-mini")
            .baseUrl("http://localhost:3065/v1")
            .timeoutMs(30000)
            .temperature(0.2)
            .build();
    }

    private void wireRecipeModel(AiModelConfig config, long version) {
        when(dynamicAiModelRegistry.current()).thenReturn(
            new DynamicAiModelRegistry.ActiveModels(config, null, null, null, null, version));
        when(aiProviderRegistry.required(config.getProvider())).thenReturn(clientFactory);
        when(clientFactory.buildRecipeModel(config)).thenReturn(recipeChatModel);
    }

    @Test
    void optimizeRecipe_putsGoalIntoPromptAndReturnsUnifiedStructure() {
        AiModelConfig config = config("openai-compatible");
        wireRecipeModel(config, 1L);
        when(recipeChatModel.chat(anyString())).thenReturn(SINGLE_PLAN_JSON);

        Map<String, Object> result = service.optimizeRecipe("番茄炒蛋", "fat_loss");

        assertFalse((Boolean) result.get("error"));
        assertEquals("fat_loss", result.get("goal"));

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipeChatModel).chat(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertTrue(prompt.contains("fat_loss"), "Prompt 应带目标标识");
        assertTrue(prompt.contains("减脂"), "Prompt 应带目标说明");
        assertTrue(prompt.contains("番茄炒蛋"), "Prompt 应包含用户食谱");

        @SuppressWarnings("unchecked")
        Map<String, Object> recipe = (Map<String, Object>) result.get("recipe");
        assertEquals("低脂番茄炒蛋", recipe.get("name"));

        @SuppressWarnings("unchecked")
        Map<String, Object> nutrition = (Map<String, Object>) result.get("nutrition");
        @SuppressWarnings("unchecked")
        Map<String, Object> original = (Map<String, Object>) nutrition.get("original");
        @SuppressWarnings("unchecked")
        Map<String, Object> optimized = (Map<String, Object>) nutrition.get("optimized");
        assertEquals(240, original.get("calorie"));
        assertEquals(160, optimized.get("calorie"));

        @SuppressWarnings("unchecked")
        List<Object> improvements = (List<Object>) result.get("improvements");
        assertEquals(1, improvements.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> improvement = (Map<String, Object>) improvements.get(0);
        assertEquals("减少用油", improvement.get("title"));
        assertEquals("油量由 15g 降至 5g", improvement.get("detail"));
        assertEquals("营养数值为 AI 估算，仅供参考", result.get("estimationNote"));
    }

    @Test
    void optimizeRecipe_arrayResponse_keepsOnlyFirstPlan() {
        AiModelConfig config = config("openai-compatible");
        wireRecipeModel(config, 1L);
        when(recipeChatModel.chat(anyString())).thenReturn("""
            [
              {"recipe": {"name": "方案一"}, "improvements": ["只保留第一条"]},
              {"recipe": {"name": "方案二"}}
            ]
            """);

        Map<String, Object> result = service.optimizeRecipe("番茄炒蛋", "balanced");

        assertFalse((Boolean) result.get("error"));
        @SuppressWarnings("unchecked")
        Map<String, Object> recipe = (Map<String, Object>) result.get("recipe");
        assertEquals("方案一", recipe.get("name"));
        @SuppressWarnings("unchecked")
        List<Object> improvements = (List<Object>) result.get("improvements");
        assertEquals(List.of("只保留第一条"), improvements);
    }

    @Test
    void optimizeRecipe_missingRecipeField_isFailure() {
        AiModelConfig config = config("openai-compatible");
        wireRecipeModel(config, 1L);
        when(recipeChatModel.chat(anyString())).thenReturn("{\"goal\":\"balanced\"}");

        Map<String, Object> result = service.optimizeRecipe("番茄炒蛋", "balanced");

        assertEquals(true, result.get("error"));
        assertTrue(((String) result.get("message")).contains("没有找到合适的优化食谱"));
    }

    @Test
    void optimizeRecipeStreaming_putsGoalIntoPromptAndEmitsUnifiedResult() {
        AiModelConfig config = config("openai-compatible");
        StreamingChatModel streamingModel = mock(StreamingChatModel.class);
        when(dynamicAiModelRegistry.current()).thenReturn(
            new DynamicAiModelRegistry.ActiveModels(config, null, null, null, null, 1L));
        when(aiProviderRegistry.required("openai-compatible")).thenReturn(clientFactory);
        when(clientFactory.buildRecipeStreamingModel(config)).thenReturn(streamingModel);
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse(SINGLE_PLAN_JSON);
            handler.onCompleteResponse(ChatResponse.builder().aiMessage(AiMessage.from(SINGLE_PLAN_JSON)).build());
            return null;
        }).when(streamingModel).chat(anyString(), any(StreamingChatResponseHandler.class));

        List<String> progress = new ArrayList<>();
        boolean[] completed = {false};

        service.optimizeRecipeStreaming("番茄炒蛋", "fat_loss", progress::add, token -> { }, () -> completed[0] = true);

        assertTrue(completed[0], "应回调 onComplete");
        assertTrue(progress.stream().anyMatch(message -> message.startsWith("RESULT:")), "应推送 RESULT 事件");
        assertTrue(progress.stream().anyMatch(message -> message.contains("\"goal\":\"fat_loss\"")),
            "结果应带请求目标");
        assertTrue(progress.stream().anyMatch(message -> message.contains("低脂番茄炒蛋")), "结果应带模型方案");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(streamingModel).chat(promptCaptor.capture(), any(StreamingChatResponseHandler.class));
        assertTrue(promptCaptor.getValue().contains("fat_loss"));
    }

    @Test
    void recipeChatModel_cachesPerRuntimeVersion() {
        AiModelConfig configV1 = config("provider-a");
        AiModelConfig configV2 = config("provider-b");
        ChatModel rebuilt = mock(ChatModel.class);
        when(dynamicAiModelRegistry.current()).thenReturn(
            new DynamicAiModelRegistry.ActiveModels(configV1, null, null, null, null, 1L),
            new DynamicAiModelRegistry.ActiveModels(configV1, null, null, null, null, 1L),
            new DynamicAiModelRegistry.ActiveModels(configV2, null, null, null, null, 2L));
        when(aiProviderRegistry.required("provider-a")).thenReturn(clientFactory);
        when(aiProviderRegistry.required("provider-b")).thenReturn(clientFactory);
        when(clientFactory.buildRecipeModel(configV1)).thenReturn(recipeChatModel);
        when(clientFactory.buildRecipeModel(configV2)).thenReturn(rebuilt);

        assertSame(recipeChatModel, implementation.recipeChatModel());
        assertSame(recipeChatModel, implementation.recipeChatModel());
        assertSame(rebuilt, implementation.recipeChatModel());

        verify(clientFactory, times(1)).buildRecipeModel(configV1);
        verify(clientFactory, times(1)).buildRecipeModel(configV2);
    }

    @Test
    void recipeChatModel_withoutConfig_returnsNoopInsteadOfThrowing() {
        when(dynamicAiModelRegistry.current()).thenReturn(
            new DynamicAiModelRegistry.ActiveModels(null, null, null, null, null, 0L));

        assertInstanceOf(NoopChatModel.class, implementation.recipeChatModel());
    }
}
