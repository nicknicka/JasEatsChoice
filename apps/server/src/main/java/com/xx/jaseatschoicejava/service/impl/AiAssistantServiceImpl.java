package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.agent.config.NoopChatModel;
import com.xx.jaseatschoicejava.agent.config.NoopStreamingChatModel;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.service.AiAssistantService;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.InvalidRequestException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.exception.UnsupportedFeatureException;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.io.IOException;


/**
 * AI 饮食助手服务实现（视觉识别与特殊功能）
 *
 * 注意：以下功能已迁移到 Agent 系统
 * - chat() → NutritionAiAgent
 * - analyzeNutrition() → NutritionAiAgent
 * - recommendRecipe() → RecommendationAiAgent
 *
 * 本类保留视觉识别和特殊功能
 *

 * @since 2026-03-22
 * @updated 2026-04-08 实现真正的AI调用
 */
@Slf4j
@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    @Resource(name = "visionModel")
    private ChatModel visionModel;

    @Resource(name = "aiModel")
    private ChatModel aiModel;

    @Resource
    private DynamicAiModelRegistry dynamicAiModelRegistry;

    @Resource
    private AiProviderRegistry aiProviderRegistry;

    /** 食谱优化模型的按版本缓存，随运行时热切换自动失效。 */
    private volatile ChatModel recipeChatModelCache;
    private volatile long recipeChatModelVersion = Long.MIN_VALUE;
    private volatile StreamingChatModel recipeStreamingModelCache;
    private volatile long recipeStreamingModelVersion = Long.MIN_VALUE;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== Prompt 常量 ====================

    /**
     * 菜品识别提示词
     */
    private static final String DISH_RECOGNITION_PROMPT = """
        你是一个专业的菜品识别专家和营养师。请仔细分析这张图片。

        首先判断图片中是否包含食物/菜品。如果不是食物图片（如风景、人物、动物、物品等），请返回：
        {
          "isDish": false,
          "reason": "请上传食物图片"
        }

        如果图片中确实是食物/菜品，请返回以下JSON格式：
        {
          "isDish": true,
          "name": "菜品名称（具体且准确）",
          "calories": 估算卡路里(数字，单位大卡),
          "protein": 蛋白质含量(数字，单位克),
          "fat": 脂肪含量(数字，单位克),
          "carbs": 碳水化合物含量(数字，单位克),
          "difficulty": "难度(简单/中等/困难)",
          "preparationTime": "准备时间(如：30分钟)",
          "ingredients": ["食材1", "食材2", "食材3"],
          "tags": ["标签1", "标签2"],
          "confidence": 置信度(0-1之间的小数),
          "nutritionScore": 营养评分(1-10分),
          "nutritionEstimated": true
        }

        注意：
        1. 必须先判断是否为食物图片，isDish字段必填
        2. 卡路里和营养成分要根据菜品分量合理估算
        3. 食材列表要包含主要食材和调料
        4. 标签可以是菜系、口味、场景等
        5. nutritionEstimated 必须为 true
        6. 只返回JSON，不要其他解释文字
        """;

    /**
     * 食谱优化提示词（单方案 + 优化目标 + 统一结构）。
     */
    private static final String RECIPE_OPTIMIZATION_PROMPT = """
        你是一个专业的营养师和烹饪专家。用户提供了食材或食谱描述，请基于此给出一个优化后的食谱方案。

        用户输入：
        %s

        优化目标：%s

        请严格返回以下 JSON 对象（不要添加任何其他文字，不要用 markdown 代码块）：
        {
          "recipe": {
            "name": "食谱名称",
            "ingredients": "食材及用量列表",
            "steps": "4-6 个简洁烹饪步骤"
          },
          "nutrition": {
            "original": {"calorie": 数字, "protein": 数字, "fat": 数字, "carb": 数字},
            "optimized": {"calorie": 数字, "protein": 数字, "fat": 数字, "carb": 数字}
          },
          "improvements": [
            {"title": "改进项标题", "detail": "具体说明变化"}
          ]
        }

        要求：
        1. 只返回 1 个优化方案，不要返回多个方案
        2. 围绕优化目标调整食谱
        3. nutrition.original 为原食谱估算，nutrition.optimized 为优化后的估算
        4. improvements 返回 2-3 条，detail 说明具体变化，不必强行量化
        5. 食材搭配合理，步骤控制在 4-6 个，简洁且易于操作
        6. 只返回 JSON 对象，不要其他解释文字
        """;

    /**
     * 推荐理由生成提示词
     */
    private static final String RECOMMENDATION_REASON_PROMPT = """
        你是一个美食推荐专家。请为以下菜品生成一段吸引人的推荐理由。

        菜品名称：%s
        用户偏好：%s
        当前场景：%s

        要求：
        1. 突出菜品特色和美味
        2. 结合用户偏好和场景
        3. 语言生动有感染力
        4. 控制在50字以内

        直接返回推荐理由文字，不要JSON格式。
        """;

    // ==================== 菜品识别 ====================

    @Override
    public Map<String, Object> recognizeDish(String imageUrl) {
        // 通过URL识别暂不支持，建议使用Base64方式
        log.warn("通过URL识别暂不支持，建议使用Base64方式上传图片");
        Map<String, Object> result = new HashMap<>();
        result.put("error", true);
        result.put("message", "请使用图片上传方式进行识别");
        return result;
    }

    @Override
    public Map<String, Object> recognizeDishWithBase64(String imageBase64, String mimeType) {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            log.error("图片Base64数据为空");
            return Map.of("error", true, "message", "图片数据不能为空");
        }

        if (visionModel == null) {
            return Map.of("error", true, "message", "视觉识别暂未开放：未配置视觉模型");
        }

        try {
            log.info("开始调用视觉模型进行菜品识别，Base64长度: {}, MIME: {}", imageBase64.length(), mimeType);

            UserMessage userMessage = UserMessage.from(
                    TextContent.from(DISH_RECOGNITION_PROMPT),
                    ImageContent.from(imageBase64, mimeType)
            );

            ChatResponse chatResponse = visionModel.chat(userMessage);
            String responseText = chatResponse.aiMessage().text();
            if (responseText == null || responseText.isBlank()) {
                return Map.of("error", true, "message", "视觉模型返回空结果，请稍后重试");
            }

            log.info("视觉模型返回结果: {}", responseText.length() > 200 ? responseText.substring(0, 200) + "..." : responseText);

            Map<String, Object> result = parseDishRecognitionResult(responseText);
            if (Boolean.TRUE.equals(result.get("notDish"))) {
                log.warn("非菜品图片: {}", result.get("message"));
                return result;
            }
            if (Boolean.TRUE.equals(result.get("error"))) {
                return result;
            }

            result.put("error", false);
            log.info("菜品识别成功: {}", result.get("name"));
            return result;

        } catch (Exception e) {
            log.error("菜品识别失败", e);
            if (isVisionRejectedByModel(e)) {
                // 4xx 能力错误：模型不接受图片输入。与网络/服务端故障分开，交给上层做稳定降级。
                log.warn("视觉模型拒绝图片输入，按视觉能力不可用处理：{}", rootMessage(e));
                return Map.of("error", true, "visionUnsupported", true,
                    "message", "当前视觉模型不支持图片输入，菜品识别暂不可用");
            }
            // 超时与一般失败分开提示，前后端展示统一文案（见 spec #222 的「统一的未登录、限流与超时错误」）
            String message = e instanceof TimeoutException ? "识别超时，请稍后重试" : "菜品识别失败，请稍后重试";
            return Map.of("error", true, "message", message);
        }
    }

    /**
     * 判断异常是否代表「模型不接受图片输入」的 4xx 能力错误。
     *
     * <p>只认 400/415/422 与 {@link UnsupportedFeatureException}：401/403 是鉴权、
     * 429 是限流、5xx 与网络异常是服务端故障，都不能据此判定视觉能力不可用。
     */
    private boolean isVisionRejectedByModel(Throwable error) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (current instanceof UnsupportedFeatureException || current instanceof InvalidRequestException) {
                return true;
            }
            if (current instanceof HttpException http) {
                int status = http.statusCode();
                if (status == 400 || status == 415 || status == 422) {
                    return true;
                }
            }
        }
        return false;
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    // ==================== 食谱优化 ====================

    @Override
    public Map<String, Object> optimizeRecipe(String originalRecipe, String goal) {
        if (originalRecipe == null || originalRecipe.trim().isEmpty()) {
            log.error("原始食谱为空");
            return Map.of("error", true, "message", "食谱内容不能为空");
        }

        String normalizedGoal = normalizeRecipeGoal(goal);
        try {
            log.info("开始调用AI进行食谱优化，目标: {}，原文长度: {}", normalizedGoal, originalRecipe.length());

            String prompt = String.format(RECIPE_OPTIMIZATION_PROMPT,
                originalRecipe, describeRecipeGoal(normalizedGoal));
            String responseText = callRecipeOptimizationModel(prompt);

            if (responseText.isBlank()) {
                log.warn("食谱优化模型返回空结果");
                return Map.of("error", true, "message", "AI返回空结果，请稍后重试");
            }

            log.info("AI返回优化结果: {}", responseText.length() > 300 ? responseText.substring(0, 300) + "..." : responseText);

            Map<String, Object> result = parseRecipeResult(responseText, normalizedGoal);
            if (result == null) {
                log.warn("AI返回的食谱方案无法解析");
                return Map.of("error", true, "message", "没有找到合适的优化食谱");
            }

            result.put("error", false);
            log.info("食谱优化成功，目标: {}", normalizedGoal);
            return result;

        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return Map.of("error", true, "message", "食谱优化失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> optimizeRecipeWithProgress(String originalRecipe, String goal,
            java.util.function.Consumer<String> progressCallback) {
        if (originalRecipe == null || originalRecipe.trim().isEmpty()) {
            return Map.of("error", true, "message", "食谱内容不能为空");
        }

        String normalizedGoal = normalizeRecipeGoal(goal);
        try {
            // 阶段1：分析食谱内容
            progressCallback.accept("正在分析食谱内容...");
            log.info("[食谱优化-SSE] 阶段1：分析食谱，长度={}，目标={}", originalRecipe.length(), normalizedGoal);

            String prompt = String.format(RECIPE_OPTIMIZATION_PROMPT,
                originalRecipe, describeRecipeGoal(normalizedGoal));

            // 阶段2：调用AI优化
            progressCallback.accept("正在调用AI进行优化...");
            log.info("[食谱优化-SSE] 阶段2：调用LLM");
            long startTime = System.currentTimeMillis();

            String responseText = callRecipeOptimizationModel(prompt);

            if (responseText.isBlank()) {
                log.warn("[食谱优化-SSE] 模型返回空结果");
                return Map.of("error", true, "message", "AI返回空结果，请稍后重试");
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[食谱优化-SSE] LLM调用完成，耗时={}ms，结果长度={}", elapsed, responseText.length());

            // 阶段3：生成优化结果
            progressCallback.accept("正在生成优化结果...");
            log.info("[食谱优化-SSE] 阶段3：解析结果");

            Map<String, Object> result = parseRecipeResult(responseText, normalizedGoal);
            if (result == null) {
                return Map.of("error", true, "message", "没有找到合适的优化食谱");
            }

            result.put("error", false);
            log.info("[食谱优化-SSE] 成功，目标: {}", normalizedGoal);
            return result;

        } catch (Exception e) {
            log.error("[食谱优化-SSE] 失败", e);
            return Map.of("error", true, "message", "食谱优化失败：" + e.getMessage());
        }
    }

    @Override
    public void optimizeRecipeStreaming(String originalRecipe,
            String goal,
            java.util.function.Consumer<String> progressCallback,
            java.util.function.Consumer<String> tokenCallback,
            java.util.function.LongConsumer onComplete) {

        if (originalRecipe == null || originalRecipe.trim().isEmpty()) {
            progressCallback.accept("ERROR:INVALID_REQUEST:食谱内容不能为空");
            onComplete.accept(0L);
            return;
        }

        StreamingChatModel recipeStreamingModel = recipeStreamingModel();
        if (recipeStreamingModel == null) {
            progressCallback.accept("ERROR:AI_NOT_CONFIGURED:食谱优化暂未开放：未配置流式模型");
            onComplete.accept(0L);
            return;
        }

        String normalizedGoal = normalizeRecipeGoal(goal);

        // 阶段1：分析食谱
        progressCallback.accept("正在分析食谱内容...");
        log.info("[食谱优化-流式] 阶段1：分析食谱，长度={}，目标={}", originalRecipe.length(), normalizedGoal);

        String prompt = String.format(RECIPE_OPTIMIZATION_PROMPT,
            originalRecipe, describeRecipeGoal(normalizedGoal));

        // 阶段2：流式调用AI
        progressCallback.accept("正在调用AI进行优化...");
        log.info("[食谱优化-流式] 阶段2：流式调用LLM");
        long startTime = System.currentTimeMillis();
        java.util.concurrent.atomic.AtomicBoolean finished = new java.util.concurrent.atomic.AtomicBoolean(false);
        StringBuilder fullResponse = new StringBuilder();

        try {
            recipeStreamingModel.chat(prompt, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    if (!finished.get() && partialResponse != null && !partialResponse.isEmpty()) {
                        fullResponse.append(partialResponse);
                        tokenCallback.accept(partialResponse);
                    }
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    if (!finished.compareAndSet(false, true)) {
                        return;
                    }
                    long elapsed = System.currentTimeMillis() - startTime;
                    String responseText = fullResponse.toString();
                    if (responseText.isBlank() && completeResponse != null && completeResponse.aiMessage() != null) {
                        responseText = completeResponse.aiMessage().text();
                    }
                    log.info("[食谱优化-流式] LLM流式完成，耗时={}ms，结果长度={}", elapsed, responseText.length());

                    // 阶段3：解析结果
                    progressCallback.accept("正在生成优化结果...");
                    Map<String, Object> result = parseRecipeResult(responseText, normalizedGoal);

                    if (result == null) {
                        progressCallback.accept("ERROR:AI_RESULT_PARSE_FAILED:没有找到合适的优化食谱");
                    } else {
                        // 通过特殊前缀传递最终结果
                        try {
                            String resultJson = objectMapper.writeValueAsString(result);
                            progressCallback.accept("RESULT:" + resultJson);
                        } catch (Exception e) {
                            log.error("序列化食谱优化结果失败", e);
                            progressCallback.accept("ERROR:AI_RESULT_PARSE_FAILED:结果解析失败");
                        }
                        log.info("[食谱优化-流式] 成功，目标: {}", normalizedGoal);
                    }
                    onComplete.accept(elapsed);
                }

                @Override
                public void onError(Throwable error) {
                    emitRecipeStreamFailure(error, startTime, progressCallback, onComplete, finished);
                }
            });
        } catch (Exception error) {
            emitRecipeStreamFailure(error, startTime, progressCallback, onComplete, finished);
        }
    }

    private void emitRecipeStreamFailure(Throwable error,
            long startTime,
            java.util.function.Consumer<String> progressCallback,
            java.util.function.LongConsumer onComplete,
            java.util.concurrent.atomic.AtomicBoolean finished) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        long elapsed = System.currentTimeMillis() - startTime;
        log.error("[食谱优化-流式] LLM调用失败，耗时={}ms", elapsed, error);
        if (error instanceof TimeoutException) {
            progressCallback.accept("ERROR:AI_TIMEOUT:生成超时，请稍后重试");
        } else {
            progressCallback.accept("ERROR:AI_CALL_FAILED:AI调用失败，请稍后重试");
        }
        onComplete.accept(elapsed);
    }

    // ==================== 推荐理由生成 ====================

    @Override
    public String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context) {
        try {
            String preferences = userProfile != null ? userProfile.toString() : "无特殊偏好";
            String scene = context != null ? context.toString() : "日常用餐";

            String prompt = String.format(RECOMMENDATION_REASON_PROMPT, dishName, preferences, scene);

            return aiModel.chat(prompt).trim();

        } catch (Exception e) {
            log.error("生成推荐理由失败", e);
            return String.format("推荐【%s】给您！这是一道美味佳肴，符合您的口味偏好。", dishName);
        }
    }

    // ==================== JSON 解析工具方法 ====================

    /**
     * 解析菜品识别结果
     */
    private Map<String, Object> parseDishRecognitionResult(String responseText) {
        try {
            // 提取JSON
            String json = extractJson(responseText);

            // 解析为Map
            Map<String, Object> result = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            Object isDishObj = result.get("isDish");
            if (isDishObj == null) {
                log.warn("菜品识别结果缺少 isDish 字段");
                return Map.of("error", true, "message", "识别结果缺少菜品判定，请重试");
            }
            if (!Boolean.TRUE.equals(isDishObj)) {
                String reason = result.get("reason") instanceof String value && !value.isBlank()
                    ? value : "请上传食物图片";
                log.warn("图片不是菜品: {}", reason);
                return Map.of("error", true, "notDish", true, "message", reason);
            }

            result.put("isDish", true);
            result.put("name", result.get("name") instanceof String value && !value.isBlank() ? value : "未知菜品");
            result.put("calories", normalizeNumber(result.get("calories"), 0));
            result.put("protein", normalizeNumber(result.get("protein"), 0));
            result.put("fat", normalizeNumber(result.get("fat"), 0));
            result.put("carbs", normalizeNumber(result.get("carbs"), 0));
            result.put("difficulty", result.get("difficulty") instanceof String value && !value.isBlank() ? value : "中等");
            result.put("preparationTime", result.get("preparationTime") instanceof String value && !value.isBlank()
                ? value : "30分钟");
            result.put("ingredients", normalizeStringList(result.get("ingredients")));
            result.put("tags", normalizeStringList(result.get("tags")));
            result.put("confidence", normalizeNumber(result.get("confidence"), 0.5));
            result.put("nutritionScore", normalizeNumber(result.get("nutritionScore"), 7));
            result.put("nutritionEstimated", true);

            return result;

        } catch (IOException e) {
            log.error("解析菜品识别结果失败: {}", responseText, e);
            return Map.of("error", true, "message", "识别结果解析失败，请稍后重试");
        }
    }

    private Number normalizeNumber(Object value, Number fallback) {
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String text) {
            try {
                return Double.valueOf(text.trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private List<String> normalizeStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(item -> !item.isBlank())
                .toList();
        }
        if (value instanceof String text && !text.isBlank()) {
            return List.of(text);
        }
        return List.of();
    }

    /**
     * 解析食谱优化结果（统一单方案结构）。
     *
     * @return 统一结构；无法解析时返回 null
     */
    private Map<String, Object> parseRecipeResult(String responseText, String goal) {
        if (responseText == null || responseText.isBlank()) {
            return null;
        }
        try {
            String json = extractJson(responseText);

            Map<String, Object> raw;
            if (json.trim().startsWith("[")) {
                // 防御：模型若仍返回数组，只接受第一个方案
                List<Map<String, Object>> list = objectMapper.readValue(
                    json, new TypeReference<List<Map<String, Object>>>() {});
                if (list.isEmpty()) {
                    return null;
                }
                raw = list.get(0);
            } else {
                raw = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            }

            Object recipeObj = raw.get("recipe");
            if (!(recipeObj instanceof Map<?, ?> recipeRaw)) {
                log.warn("食谱优化结果缺少 recipe 字段");
                return null;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("goal", stringOr(raw.get("goal"), goal));

            Map<String, Object> recipe = new LinkedHashMap<>();
            recipe.put("name", stringOr(recipeRaw.get("name"), "AI 优化食谱"));
            recipe.put("difficulty", stringOr(recipeRaw.get("difficulty"), "中等"));
            recipe.put("ingredients", stringOr(recipeRaw.get("ingredients"), ""));
            recipe.put("steps", stringOr(recipeRaw.get("steps"), ""));
            result.put("recipe", recipe);

            Map<String, Object> nutrition = new LinkedHashMap<>();
            nutrition.put("original", normalizeMacros(nutritionSection(raw.get("nutrition"), "original")));
            nutrition.put("optimized", normalizeMacros(nutritionSection(raw.get("nutrition"), "optimized")));
            result.put("nutrition", nutrition);

            result.put("improvements", normalizeImprovements(raw.get("improvements")));
            result.put("estimationNote", stringOr(raw.get("estimationNote"), "营养数值为 AI 估算，仅供参考"));
            return result;

        } catch (IOException e) {
            log.error("解析食谱结果失败: {}",
                responseText.substring(0, Math.min(200, responseText.length())), e);
            return null;
        }
    }

    private Object nutritionSection(Object nutritionObj, String key) {
        if (nutritionObj instanceof Map<?, ?> nutrition) {
            return nutrition.get(key);
        }
        return null;
    }

    private Map<String, Object> normalizeMacros(Object node) {
        Map<?, ?> source = node instanceof Map<?, ?> map ? map : Map.of();
        Map<String, Object> macros = new LinkedHashMap<>();
        macros.put("calorie", normalizeNumber(source.get("calorie"), 0));
        macros.put("protein", normalizeNumber(source.get("protein"), 0));
        macros.put("fat", normalizeNumber(source.get("fat"), 0));
        macros.put("carb", normalizeNumber(source.get("carb"), 0));
        return macros;
    }

    private List<Object> normalizeImprovements(Object value) {
        if (!(value instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<Object> improvements = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof String text) {
                if (!text.isBlank()) {
                    improvements.add(text);
                }
            } else if (item instanceof Map<?, ?> map) {
                String title = stringOr(map.get("title"), "");
                String detail = stringOr(map.get("detail"), "");
                if (!title.isBlank() || !detail.isBlank()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("title", title);
                    entry.put("detail", detail);
                    improvements.add(entry);
                }
            }
        }
        return improvements;
    }

    private String stringOr(Object value, String fallback) {
        return value instanceof String text && !text.isBlank() ? text : fallback;
    }

    /**
     * 从响应文本中提取JSON
     * 处理可能的markdown代码块格式
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) {
            return "{}";
        }

        String trimmed = text.trim();

        // 处理 ```json ... ``` 格式
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 处理 ``` ... ``` 格式
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            // 跳过可能的语言标识
            while (start < trimmed.length() && !Character.isWhitespace(trimmed.charAt(start)) && trimmed.charAt(start) != '{') {
                start++;
            }
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 尝试找到JSON对象的起始和结束
        int jsonStart = trimmed.indexOf('{');
        int jsonEnd = trimmed.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return trimmed.substring(jsonStart, jsonEnd + 1);
        }

        return trimmed;
    }

    /**
     * 调用食谱优化专用对话模型（独立温度、输出上限与超时）。
     */
    private String callRecipeOptimizationModel(String prompt) {
        ChatModel recipeModel = recipeChatModel();
        if (recipeModel == null) {
            return "";
        }
        String responseText = recipeModel.chat(prompt);
        if (responseText == null || responseText.isBlank()) {
            log.warn("食谱优化模型返回空结果");
            return "";
        }
        return responseText.trim();
    }

    /**
     * 按运行时配置版本缓存食谱优化对话模型，管理员热切换后自动重建。
     */
    ChatModel recipeChatModel() {
        DynamicAiModelRegistry.ActiveModels active = dynamicAiModelRegistry.current();
        AiModelConfig config = active.config();
        if (config == null) {
            return new NoopChatModel();
        }
        long version = active.version();
        ChatModel cached = recipeChatModelCache;
        if (cached != null && version == recipeChatModelVersion) {
            return cached;
        }
        synchronized (this) {
            if (recipeChatModelCache == null || recipeChatModelVersion != version) {
                recipeChatModelCache = aiProviderRegistry.required(config.getProvider())
                    .buildRecipeModel(config);
                recipeChatModelVersion = version;
            }
            return recipeChatModelCache;
        }
    }

    /**
     * 按运行时配置版本缓存食谱优化流式模型，管理员热切换后自动重建。
     */
    StreamingChatModel recipeStreamingModel() {
        DynamicAiModelRegistry.ActiveModels active = dynamicAiModelRegistry.current();
        AiModelConfig config = active.config();
        if (config == null) {
            return new NoopStreamingChatModel();
        }
        long version = active.version();
        StreamingChatModel cached = recipeStreamingModelCache;
        if (cached != null && version == recipeStreamingModelVersion) {
            return cached;
        }
        synchronized (this) {
            if (recipeStreamingModelCache == null || recipeStreamingModelVersion != version) {
                recipeStreamingModelCache = aiProviderRegistry.required(config.getProvider())
                    .buildRecipeStreamingModel(config);
                recipeStreamingModelVersion = version;
            }
            return recipeStreamingModelCache;
        }
    }

    private String normalizeRecipeGoal(String goal) {
        return goal == null || goal.isBlank() ? AiAssistantService.DEFAULT_RECIPE_GOAL : goal.trim();
    }

    private String describeRecipeGoal(String goal) {
        return switch (goal) {
            case "fat_loss" -> "减脂（fat_loss）：降低总热量与脂肪，提升饱腹感";
            case "muscle_gain" -> "增肌（muscle_gain）：提高蛋白质与总热量";
            case "blood_sugar_control" -> "控糖（blood_sugar_control）：降低精制碳水与升糖负担";
            default -> "营养均衡（balanced）：平衡蛋白质、脂肪与碳水";
        };
    }
}
