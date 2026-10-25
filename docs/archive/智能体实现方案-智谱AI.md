# 佳食宜选 - 智谱AI智能体实现方案

## 一、技术架构

### 1.1 核心依赖
```xml
<!-- pom.xml -->
<dependencies>
    <!-- 智谱AI SDK -->
    <dependency>
        <groupId>com.zhipu</groupId>
        <artifactId>zhipu-ai-sdk</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- HTTP客户端 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <!-- Redis（记忆存储） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- 向量数据库（可选，用于长期记忆） -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-vectorstore</artifactId>
    </dependency>
</dependencies>
```

### 1.2 配置文件
```yaml
# application.yml
zhipu:
  ai:
    api-key: ${ZHIPU_API_KEY}
    model: glm-4-plus  # 或 glm-4.6v (多模态)
    base-url: https://open.bigmodel.cn/api
    temperature: 0.7
    max-tokens: 2000
    top-p: 0.9

spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

## 二、核心代码实现

### 2.1 智谱AI客户端配置
```java
package com.jiashiyixuan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.Data;

/**
 * 智谱AI配置
 */
@Configuration
@Data
public class ZhipuAIConfig {

    @Value("${zhipu.ai.api-key}")
    private String apiKey;

    @Value("${zhipu.ai.model}")
    private String model;

    @Value("${zhipu.ai.base-url}")
    private String baseUrl;

    @Value("${zhipu.ai.temperature}")
    private Double temperature;

    @Value("${zhipu.ai.max-tokens}")
    private Integer maxTokens;

    @Bean
    public WebClient zhipuWebClient() {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
```

### 2.2 Function Call 工具定义
```java
package com.jiashiyixuan.ai.tool;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * AI工具定义
 */
@Data
public class AITool {

    private String name;
    private String description;
    private Map<String, Parameter> parameters;

    @Data
    public static class Parameter {
        private String type;
        private String description;
        private Boolean required;
        private Object defaultValue;

        @JsonProperty("enum")
        private List<String> enumValues;
    }
}

/**
 * 营养查询工具
 */
public class NutritionTool extends AITool {

    public NutritionTool() {
        this.setName("calculate_nutrition");
        this.setDescription("计算食物的营养成分和卡路里");

        Map<String, Parameter> params = new HashMap<>();
        params.put("foodName", new Parameter() {{
            setType("string");
            setDescription("食物名称");
            setRequired(true);
        }});
        params.put("amount", new Parameter() {{
            setType("number");
            setDescription("食用量（克）");
            setRequired(true);
            setDefaultValue(100.0);
        }});

        this.setParameters(params);
    }
}

/**
 * 食谱搜索工具
 */
public class RecipeSearchTool extends AITool {

    public RecipeSearchTool() {
        this.setName("search_recipe");
        this.setDescription("根据需求和偏好搜索食谱");

        Map<String, Parameter> params = new HashMap<>();
        params.put("preference", new Parameter() {{
            setType("string");
            setDescription("饮食偏好（低脂/高蛋白/素食等）");
            setRequired(false);
        }});
        params.put("calorieLimit", new Parameter() {{
            setType("number");
            setDescription("卡路里上限");
            setRequired(false);
        }});
        params.put("mealType", new Parameter() {{
            setType("string");
            setDescription("餐型（早餐/午餐/晚餐）");
            setRequired(false);
            setEnumValues(Arrays.asList("早餐", "午餐", "晚餐", "加餐"));
        }});

        this.setParameters(params);
    }
}

/**
 * 订单创建工具
 */
public class OrderTool extends AITool {

    public OrderTool() {
        this.setName("create_order");
        this.setDescription("为用户创建订单");

        Map<String, Parameter> params = new HashMap<>();
        params.put("dishes", new Parameter() {{
            setType("array");
            setDescription("菜品列表");
            setRequired(true);
        }});
        params.put("address", new Parameter() {{
            setType("string");
            setDescription("配送地址");
            setRequired(true);
        }});
        params.put("remark", new Parameter() {{
            setType("string");
            setDescription("备注");
            setRequired(false);
        }});

        this.setParameters(params);
    }
}
```

### 2.3 Agent服务实现
```java
package com.jiashiyixuan.ai.service;

import com.jiashiyixuan.ai.tool.*;
import com.jiashiyixuan.config.ZhipuAIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI智能体服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAgentService {

    private final WebClient zhipuWebClient;
    private final ZhipuAIConfig config;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 工具注册表
    private final Map<String, AITool> tools = new HashMap<>();

    @PostConstruct
    public void init() {
        // 注册所有可用工具
        tools.put("calculate_nutrition", new NutritionTool());
        tools.put("search_recipe", new RecipeSearchTool());
        tools.put("create_order", new OrderTool());
    }

    /**
     * Agent对话（支持Function Call）
     */
    public AgentResponse chat(AgentRequest request) {
        String userId = request.getUserId();
        String userMessage = request.getMessage();

        log.info("用户 {} 发送消息: {}", userId, userMessage);

        // 1. 加载用户记忆（从Redis）
        List<Message> history = loadConversationHistory(userId);

        // 2. 构建请求
        ZhipuChatRequest chatRequest = buildChatRequest(userMessage, history);

        // 3. 调用智谱API
        ZhipuChatResponse chatResponse = callZhipuAPI(chatRequest);

        // 4. 处理Function Call
        AgentResponse response = processResponse(chatResponse, userId);

        // 5. 保存对话历史
        saveConversationHistory(userId, userMessage, response);

        return response;
    }

    /**
     * 流式对话（SSE）
     */
    public void chatStream(AgentRequest request, StreamCallback callback) {
        String userId = request.getUserId();
        List<Message> history = loadConversationHistory(userId);

        ZhipuChatRequest chatRequest = buildChatRequest(request.getMessage(), history);
        chatRequest.setStream(true);

        zhipuWebClient.post()
            .uri("/paas/v4/chat/completions")
            .bodyValue(chatRequest)
            .retrieve()
            .bodyToFlux(String.class)
            .subscribe(
                chunk -> {
                    // 处理流式响应
                    callback.onChunk(parseStreamChunk(chunk));
                },
                error -> {
                    log.error("流式对话失败", error);
                    callback.onError(error);
                },
                () -> {
                    log.info("流式对话完成");
                    callback.onComplete();
                }
            );
    }

    /**
     * 构建聊天请求
     */
    private ZhipuChatRequest buildChatRequest(String message, List<Message> history) {
        ZhipuChatRequest request = new ZhipuChatRequest();
        request.setModel(config.getModel());
        request.setTemperature(config.getTemperature());
        request.setMaxTokens(config.getMaxTokens());
        request.setTopP(config.getTopP());

        // 构建消息列表
        List<ChatMessage> messages = new ArrayList<>();

        // 系统提示词
        messages.add(new ChatMessage("system", buildSystemPrompt()));

        // 历史消息
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        // 当前消息
        messages.add(new ChatMessage("user", message));

        request.setMessages(messages);

        // 注册工具
        request.setTools(new ArrayList<>(tools.values()));

        return request;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
            你是"佳食宜选"的AI饮食助手，专注于帮助用户做出健康的饮食选择。

            核心能力：
            1. 营养分析：计算食物的卡路里、蛋白质、脂肪、碳水等营养成分
            2. 食谱推荐：根据用户偏好、健康目标推荐合适的食谱
            3. 饮食规划：制定个性化的饮食计划和购物清单
            4. 订单协助：帮助用户下单和跟踪订单状态

            交互原则：
            - 友好专业，使用易懂的语言
            - 主动询问关键信息（过敏源、饮食偏好、健康目标）
            - 给出具体可操作的建议
            - 使用工具获取准确信息
            - 记住用户偏好，提供个性化服务

            可用工具：
            - calculate_nutrition: 计算营养成分
            - search_recipe: 搜索食谱
            - create_order: 创建订单

            使用工具时，确保参数完整准确。如果缺少必要信息，先询问用户。
            """;
    }

    /**
     * 调用智谱API
     */
    private ZhipuChatResponse callZhipuAPI(ZhipuChatRequest request) {
        try {
            return zhipuWebClient.post()
                .uri("/paas/v4/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ZhipuChatResponse.class)
                .block();
        } catch (Exception e) {
            log.error("调用智谱API失败", e);
            throw new RuntimeException("AI服务暂时不可用", e);
        }
    }

    /**
     * 处理API响应（包括Function Call）
     */
    private AgentResponse processResponse(ZhipuChatResponse chatResponse, String userId) {
        AgentResponse response = new AgentResponse();

        ChatMessage message = chatResponse.getChoices().get(0).getMessage();

        // 检查是否有Function Call
        if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
            // 执行工具调用
            List<ToolResult> toolResults = new ArrayList<>();

            for (ToolCall toolCall : message.getToolCalls()) {
                ToolResult result = executeTool(toolCall, userId);
                toolResults.add(result);
            }

            // 将工具结果传回模型，生成最终回复
            response = generateFinalResponse(chatResponse, toolResults, userId);
        } else {
            // 直接返回文本回复
            response.setMessage(message.getContent());
            response.setType("text");
        }

        return response;
    }

    /**
     * 执行工具调用
     */
    private ToolResult executeTool(ToolCall toolCall, String userId) {
        String toolName = toolCall.getFunction().getName();
        Map<String, Object> arguments = toolCall.getFunction().getArguments();

        log.info("执行工具: {}, 参数: {}", toolName, arguments);

        try {
            switch (toolName) {
                case "calculate_nutrition":
                    return calculateNutrition(arguments);

                case "search_recipe":
                    return searchRecipe(arguments);

                case "create_order":
                    return createOrder(arguments, userId);

                default:
                    return ToolResult.error("未知工具: " + toolName);
            }
        } catch (Exception e) {
            log.error("工具执行失败: " + toolName, e);
            return ToolResult.error("工具执行失败: " + e.getMessage());
        }
    }

    /**
     * 计算营养成分
     */
    private ToolResult calculateNutrition(Map<String, Object> args) {
        String foodName = (String) args.get("foodName");
        Double amount = (Double) args.getOrDefault("amount", 100.0);

        // 调用营养数据库
        NutritionInfo nutrition = nutritionService.query(foodName, amount);

        Map<String, Object> result = new HashMap<>();
        result.put("foodName", foodName);
        result.put("amount", amount);
        result.put("calories", nutrition.getCalories());
        result.put("protein", nutrition.getProtein());
        result.put("fat", nutrition.getFat());
        result.put("carbs", nutrition.getCarbs());

        return ToolResult.success(result);
    }

    /**
     * 搜索食谱
     */
    private ToolResult searchRecipe(Map<String, Object> args) {
        String preference = (String) args.get("preference");
        Integer calorieLimit = (Integer) args.get("calorieLimit");
        String mealType = (String) args.get("mealType");

        // 调用推荐系统
        List<Recipe> recipes = recipeService.search(preference, calorieLimit, mealType);

        Map<String, Object> result = new HashMap<>();
        result.put("recipes", recipes);
        result.put("total", recipes.size());

        return ToolResult.success(result);
    }

    /**
     * 创建订单
     */
    private ToolResult createOrder(Map<String, Object> args, String userId) {
        List<Map<String, Object>> dishes = (List<Map<String, Object>>) args.get("dishes");
        String address = (String) args.get("address");
        String remark = (String) args.getOrDefault("remark", "");

        // 调用订单服务
        Order order = orderService.create(userId, dishes, address, remark);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("totalAmount", order.getTotalAmount());
        result.put("estimatedTime", order.getEstimatedTime());

        return ToolResult.success(result);
    }

    /**
     * 生成最终响应
     */
    private AgentResponse generateFinalResponse(
        ZhipuChatResponse originalResponse,
        List<ToolResult> toolResults,
        String userId
    ) {
        // 构建新的请求，包含工具结果
        List<ChatMessage> messages = new ArrayList<>(originalResponse.getMessages());

        // 添加助手消息（包含tool_calls）
        messages.add(originalResponse.getChoices().get(0).getMessage());

        // 添加工具结果消息
        for (ToolResult result : toolResults) {
            ChatMessage toolMessage = new ChatMessage();
            toolMessage.setRole("tool");
            toolMessage.setToolCallId(result.getToolCallId());
            toolMessage.setContent(objectMapper.writeValueAsString(result.getData()));
            messages.add(toolMessage);
        }

        // 再次调用模型
        ZhipuChatRequest newRequest = new ZhipuChatRequest();
        newRequest.setModel(config.getModel());
        newRequest.setMessages(messages);

        ZhipuChatResponse finalResponse = callZhipuAPI(newRequest);

        AgentResponse response = new AgentResponse();
        response.setMessage(finalResponse.getChoices().get(0).getMessage().getContent());
        response.setType("assistant");

        // 如果有推荐结果，添加到响应
        if (toolResults.stream().anyMatch(r -> r.getData().containsKey("recipes"))) {
            response.setSuggestions(extractSuggestions(toolResults));
        }

        return response;
    }

    /**
     * 加载对话历史
     */
    private List<Message> loadConversationHistory(String userId) {
        String key = "chat:history:" + userId;
        List<Object> history = redisTemplate.opsForList().range(key, 0, -1);

        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }

        return history.stream()
            .map(obj -> (Message) obj)
            .toList();
    }

    /**
     * 保存对话历史
     */
    private void saveConversationHistory(String userId, String userMessage, AgentResponse response) {
        String key = "chat:history:" + userId;

        // 保存用户消息
        Message userMsg = new Message("user", userMessage);
        redisTemplate.opsForList().rightPush(key, userMsg);

        // 保存助手回复
        Message assistantMsg = new Message("assistant", response.getMessage());
        redisTemplate.opsForList().rightPush(key, assistantMsg);

        // 设置过期时间（7天）
        redisTemplate.expire(key, 7, TimeUnit.DAYS);

        // 限制历史长度（最近50条）
        redisTemplate.opsForList().trim(key, -50, -1);
    }

    /**
     * 清空对话历史
     */
    public void clearHistory(String userId) {
        String key = "chat:history:" + userId;
        redisTemplate.delete(key);
    }
}
```

### 2.4 控制器层
```java
package com.jiashiyixuan.controller;

import com.jiashiyixuan.ai.service.AIAgentService;
import com.jiashiyixuan.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * AI智能体控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/agent")
@RequiredArgsConstructor
public class AIAgentController {

    private final AIAgentService agentService;

    /**
     * 普通对话接口
     */
    @PostMapping("/chat")
    public Result<AgentResponse> chat(@RequestBody AgentRequest request) {
        AgentResponse response = agentService.chat(request);
        return Result.success(response);
    }

    /**
     * 流式对话接口（SSE）
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AgentRequest request) {
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时

        agentService.chatStream(request, new StreamCallback() {
            @Override
            public void onChunk(String chunk) {
                try {
                    emitter.send(SseEmitter.event().data(chunk));
                } catch (IOException e) {
                    log.error("发送SSE消息失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式对话失败", error);
                emitter.completeWithError(error);
            }

            @Override
            public void onComplete() {
                emitter.complete();
            }
        });

        return emitter;
    }

    /**
     * 清空对话历史
     */
    @DeleteMapping("/history/{userId}")
    public Result<Void> clearHistory(@PathVariable String userId) {
        agentService.clearHistory(userId);
        return Result.success();
    }
}

// 请求响应对象
@Data
class AgentRequest {
    private String userId;
    private String message;
    private String conversationId;
}

@Data
class AgentResponse {
    private String type; // text / suggestion / action
    private String message;
    private List<Suggestion> suggestions;
    private Map<String, Object> data;
}

@Data
class Suggestion {
    private String title;
    private String description;
    private String action;
    private Map<String, Object> params;
}

// 流式回调接口
interface StreamCallback {
    void onChunk(String chunk);
    void onError(Throwable error);
    void onComplete();
}
```

### 2.5 智谱API响应对象
```java
package com.jiashiyixuan.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
class ZhipuChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private List<AITool> tools;
    private Boolean stream;
}

@Data
class ChatMessage {
    private String role;
    private String content;
    private String name;
    private String toolCallId;
    private List<ToolCall> toolCalls;
}

@Data
class ZhipuChatResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
}

@Data
class Choice {
    private Integer index;
    private ChatMessage message;
    private String finishReason;
}

@Data
class ToolCall {
    private Integer index;
    private String id;
    private Function function;
}

@Data
class Function {
    private String name;
    private Map<String, Object> arguments;
}

@Data
class Usage {
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
```

## 三、前端集成（UniApp）

### 3.1 Agent Composable
```javascript
// composables/useAIAgent.js
import { ref, computed } from 'vue'
import { userStore } from '@/stores/user'

export function useAIAgent() {
  const isThinking = ref(false)
  const currentTool = ref(null)
  const messages = ref([])

  /**
   * 发送消息（普通模式）
   */
  const sendMessage = async (message) => {
    isThinking.value = true

    try {
      const response = await uni.request({
        url: 'https://api.example.com/api/ai/agent/chat',
        method: 'POST',
        data: {
          userId: userStore.userInfo.id,
          message: message
        }
      })

      const data = response.data

      if (data.type === 'suggestion' && data.suggestions) {
        // 显示建议卡片
        return {
          message: data.message,
          suggestions: data.suggestions
        }
      }

      return { message: data.message }

    } catch (error) {
      console.error('Agent调用失败:', error)
      uni.showToast({
        title: 'AI服务暂时不可用',
        icon: 'none'
      })
    } finally {
      isThinking.value = false
    }
  }

  /**
   * 发送消息（流式模式）
   */
  const sendMessageStream = async (message, onChunk) => {
    isThinking.value = true

    return new Promise((resolve, reject) => {
      const task = plus.stream.createStreamTask({
        url: 'https://api.example.com/api/ai/agent/chat/stream',
        method: 'POST',
        data: {
          userId: userStore.userInfo.id,
          message: message
        },
        headers: {
          'Content-Type': 'application/json'
        },
        success: (response) => {
          isThinking.value = false
          resolve(response)
        },
        fail: (error) => {
          isThinking.value = false
          reject(error)
        }
      })

      // 监听数据块
      task.onDataReceived((chunk) => {
        const data = JSON.parse(chunk)

        if (data.type === 'tool_call') {
          currentTool.value = data.tool
        }

        if (data.type === 'content') {
          onChunk(data.content)
        }
      })
    })
  }

  return {
    isThinking,
    currentTool,
    messages,
    sendMessage,
    sendMessageStream
  }
}
```

### 3.2 AI对话页面（改进版）
```vue
<template>
  <view class="ai-chat-container">
    <!-- 聊天头部 -->
    <view class="chat-header">
      <view class="header-info">
        <text class="header-title">AI饮食助手</text>
        <text class="header-status">
          {{ isThinking ? '思考中...' : '在线' }}
        </text>
      </view>
    </view>

    <!-- 工具调用提示 -->
    <view class="tool-indicator" v-if="currentTool">
      <text class="tool-icon">⚙️</text>
      <text class="tool-text">正在使用: {{ getToolName(currentTool) }}</text>
    </view>

    <!-- 消息列表 -->
    <scroll-view class="chat-messages" scroll-y>
      <view
        class="message-wrapper"
        v-for="(msg, index) in messages"
        :key="index"
      >
        <view class="message" :class="{ user: msg.isUser }">
          <view class="message-content">
            <text class="content-text">{{ msg.content }}</text>

            <!-- 建议卡片 -->
            <view class="suggestions" v-if="msg.suggestions">
              <view
                class="suggestion-card"
                v-for="(suggestion, sIndex) in msg.suggestions"
                :key="sIndex"
                @click="applySuggestion(suggestion)"
              >
                <text class="suggestion-title">{{ suggestion.title }}</text>
                <text class="suggestion-desc">{{ suggestion.description }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="chat-input-area">
      <input
        class="chat-input"
        v-model="inputText"
        placeholder="输入您的问题..."
        @confirm="handleSend"
      />
      <button class="send-btn" @click="handleSend">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useAIAgent } from '@/composables/useAIAgent'

const { isThinking, currentTool, sendMessage } = useAIAgent()

const inputText = ref('')
const messages = ref([])

const handleSend = async () => {
  if (!inputText.value.trim()) return

  const userMessage = inputText.value
  messages.value.push({
    content: userMessage,
    isUser: true
  })

  inputText.value = ''

  const response = await sendMessage(userMessage)

  messages.value.push({
    content: response.message,
    suggestions: response.suggestions,
    isUser: false
  })
}

const getToolName = (toolKey) => {
  const toolNames = {
    'calculate_nutrition': '营养计算',
    'search_recipe': '食谱搜索',
    'create_order': '订单创建'
  }
  return toolNames[toolKey] || toolKey
}
</script>
```

## 四、高级功能

### 4.1 多模态能力（图片识别）
```java
/**
 * 图片识别（GLM-4V）
 */
public ToolResult analyzeImage(String imageUrl, String userId) {
    ZhipuChatRequest request = new ZhipuChatRequest();
    request.setModel("glm-4v"); // 视觉模型

    List<ChatMessage> messages = new ArrayList<>();
    ChatMessage message = new ChatMessage();
    message.setRole("user");

    // 多模态内容
    List<Map<String, Object>> content = new ArrayList<>();

    // 文本
    Map<String, Object> textContent = new HashMap<>();
    textContent.put("type", "text");
    textContent.put("text", "请识别这张图片中的菜品，并分析其营养成分");
    content.add(textContent);

    // 图片
    Map<String, Object> imageContent = new HashMap<>();
    imageContent.put("type", "image_url");
    Map<String, String> imageUrlMap = new HashMap<>();
    imageUrlMap.put("url", imageUrl);
    imageContent.put("image_url", imageUrlMap);
    content.add(imageContent);

    message.setMultimodalContent(content);
    messages.add(message);

    request.setMessages(messages);

    ZhipuChatResponse response = callZhipuAPI(request);

    return ToolResult.success(response.getChoices().get(0).getMessage().getContent());
}
```

### 4.2 智能规划（Agent自主决策）
```java
/**
 * 复杂任务规划
 */
public AgentResponse planMeal(String userId, MealPlanRequest request) {
    // 1. 获取用户画像
    UserProfile profile = getUserProfile(userId);

    // 2. 调用Agent进行规划
    String planningPrompt = String.format("""
        为用户制定一周饮食计划：

        用户信息：
        - 姓名：%s
        - 健康目标：%s
        - 过敏源：%s
        - 饮食偏好：%s
        - 预算：%s元/天

        请使用工具搜索合适的食谱，并生成完整的购物清单。
        """,
        profile.getName(),
        request.getHealthGoal(),
        profile.getAllergies(),
        profile.getPreferences(),
        request.getBudget()
    );

    return chat(AgentRequest.builder()
        .userId(userId)
        .message(planningPrompt)
        .build());
}
```

## 五、成本对比

### 5.1 定价对比（2026年）
| 模型 | 输入价格 | 输出价格 | 优势 |
|------|---------|---------|------|
| GLM-4-Plus | ¥0.05/千tokens | ¥0.1/千tokens | 综合能力强 |
| GLM-4-Flash | ¥0.005/千tokens | ¥0.01/千tokens | 超低成本 |
| GPT-4o | ¥0.15/千tokens | ¥0.3/千tokens | 能力最强 |
| 文心一言 | ¥0.04/千tokens | ¥0.08/千tokens | 中文优化 |

**结论**：智谱GLM性价比最高，比GPT-4便宜约70%

### 5.2 月度成本估算（1万日活）
- GLM-4-Flash：约 ¥500-1000/月
- GLM-4-Plus：约 ¥3000-5000/月
- GPT-4o：约 ¥10000-15000/月

## 六、实施步骤

### 阶段一：基础集成（1周）
1. 申请智谱AI API Key
2. 完成SDK集成
3. 实现基础对话功能
4. 测试Function Call

### 阶段二：工具开发（1-2周）
1. 开发营养查询工具
2. 开发食谱搜索工具
3. 开发订单创建工具
4. 工具测试与优化

### 阶段三：前端集成（1周）
1. 集成流式对话
2. 实现工具调用UI
3. 添加建议卡片
4. 优化用户体验

### 阶段四：优化上线（1周）
1. 性能优化
2. 错误处理
3. 监控告警
4. 灰度发布

**总计：4-5周完成完整智能体功能**

## 七、参考资源

- [智谱AI官方文档](https://open.bigmodel.cn/dev/api)
- [GLM-4 Function Call文档](https://docs.bigmodel.cn/cn/guide/capabilities/function-calling)
- [智谱MCP协议教程](https://www.cnbugs.com/post-7185.html)
- [Spring AI集成指南](https://docs.spring.io/spring-ai/reference/)
