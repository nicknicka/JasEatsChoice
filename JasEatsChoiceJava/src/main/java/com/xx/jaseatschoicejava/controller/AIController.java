package com.xx.jaseatschoicejava.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.dto.DishDescriptionRequestDTO;
import com.xx.jaseatschoicejava.service.DishDescriptionService;
import com.xx.jaseatschoicejava.service.ZhipuAIService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;

/**
 * AI助手控制器（活跃端点）
 *
 * 仅保留菜品识别、食谱优化、健康检查等独立功能。
 * AI对话统一走 SupervisorSSEController（/agent/supervisor-sse/chat）。
 *
 * @author Claude
 * @since 2026-03-22
 * @updated 2026-04-03 清理废弃端点，保留活跃功能
 */
@Api(tags = "AI助手（独立功能）")
@RestController
@RequestMapping("/v1/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);
    private static final long SSE_TIMEOUT_MS = 300000L;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService recipeExecutorService = Executors.newCachedThreadPool();

    @Resource
    private ZhipuAIService zhipuAIService;

    @Resource
    private FileUploadConfig fileUploadConfig;

    @Resource
    private DishDescriptionService dishDescriptionService;

    // ==================== 菜品识别 ====================

    /**
     * AI菜品识别接口
     *
     * 路由: /v1/ai/dish-recognize
     * 返回: ResponseResult (识别结果)
     */
    @ApiOperation(value = "AI菜品识别", notes = "使用GLM-4V模型识别菜品图片")
    @PostMapping(value = "/dish-recognize", consumes = "multipart/form-data")
    public ResponseResult<?> dishRecognize(@RequestParam("image") MultipartFile image,
                                          @RequestParam(value = "userId", required = false) String userId) {
        try {
            log.info("接收到菜品识别请求，文件名：{}, 大小：{}", image.getOriginalFilename(), image.getSize());

            // 1. 读取图片字节数据
            byte[] imageBytes;
            try {
                imageBytes = image.getBytes();
            } catch (IOException e) {
                log.error("读取上传图片失败", e);
                return ResponseResult.fail("500", "读取上传图片失败：" + e.getMessage());
            }

            // 2. 将图片转为Base64编码（用于AI识别）
            String base64Data = java.util.Base64.getEncoder().encodeToString(imageBytes);
            log.info("图片Base64编码长度：{} 字符", base64Data.length());

            // 3. 保存图片文件
            String uploadDir = fileUploadConfig.getUploadPath() + "dish-recognition/";
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = image.getOriginalFilename();
            String suffix = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = java.util.UUID.randomUUID().toString() + suffix;
            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
            try {
                java.nio.file.Files.write(filePath, imageBytes);
            } catch (IOException e) {
                log.error("保存上传图片失败", e);
                return ResponseResult.fail("500", "保存上传图片失败：" + e.getMessage());
            }

            // 4. 构建图片URL
            String imageUrl = fileUploadConfig.getServerUrl() + "/" + fileUploadConfig.getUrlPrefix() + "dish-recognition/" + fileName;
            log.info("图片上传成功，URL：{}", imageUrl);

            // 5. 调用AI识别服务
            Map<String, Object> result = zhipuAIService.recognizeDishWithBase64(base64Data);

            // 6. 检查识别结果
            if (result == null) {
                log.error("AI识别返回null结果");
                return ResponseResult.fail("500", "菜品识别失败：服务返回空结果");
            }

            if (Boolean.TRUE.equals(result.get("error"))) {
                String errorMessage = (String) result.get("message");
                log.error("AI识别失败：{}", errorMessage);
                // 非菜品图片，返回特定错误码
                if (Boolean.TRUE.equals(result.get("notDish"))) {
                    return ResponseResult.fail("4001", errorMessage != null ? errorMessage : "请上传菜品图片");
                }
                return ResponseResult.fail("500", errorMessage != null ? errorMessage : "菜品识别失败");
            }

            result.put("imageUrl", imageUrl);
            return ResponseResult.success(result);
        } catch (RuntimeException e) {
            log.error("菜品识别失败", e);
            return ResponseResult.fail("500", "菜品识别失败：" + e.getMessage());
        }
    }

    /**
     * 兼容旧接口：通过JSON传图片URL识别
     */
    @PostMapping("/dish-recognize-url")
    public ResponseResult<?> dishRecognizeByUrl(@RequestBody Map<String, Object> params) {
        try {
            String imageUrl = (String) params.get("imageUrl");
            Map<String, Object> result = zhipuAIService.recognizeDish(imageUrl);
            return ResponseResult.success(result);
        } catch (RuntimeException e) {
            log.error("菜品识别失败", e);
            return ResponseResult.fail("500", "菜品识别失败：" + e.getMessage());
        }
    }

    // ==================== 食谱优化 ====================

    /**
     * AI食谱优化接口
     *
     * 路由: /v1/ai/recipe-upload
     */
    @ApiOperation(value = "AI食谱优化", notes = "上传食谱进行优化建议")
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params) {
        try {
            Object recipeObj = params.get("recipe");
            if (!(recipeObj instanceof Map<?, ?> recipe)) {
                return ResponseResult.fail("400", "食谱内容格式不正确");
            }

            Object recipeTextObj = recipe.get("text");
            String recipeText = recipeTextObj != null ? recipeTextObj.toString() : null;
            if (recipeText == null || recipeText.trim().isEmpty()) {
                return ResponseResult.fail("400", "食谱内容不能为空");
            }

            Map<String, Object> result = zhipuAIService.optimizeRecipe(recipeText);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return ResponseResult.fail("500", "食谱优化失败：" + e.getMessage());
        }
    }

    // ==================== 食谱优化（前端统一入口） ====================

    /**
     * AI食谱优化接口（前端统一调用）
     *
     * 路由: POST /v1/ai/recipe
     * 接收: { "recipe": "食谱内容", "goal": "优化目标" }
     * 返回: 优化后的食谱结果
     */
    @ApiOperation(value = "AI食谱优化（统一入口）", notes = "根据食材或食谱生成优化建议")
    @PostMapping("/recipe")
    public ResponseResult<?> recipeOptimize(@RequestBody Map<String, Object> params) {
        try {
            // 兼容桌面端 {foodName: "..."} 和 UniApp {recipe: "...", goal: "..."}
            String foodName = (String) params.get("foodName");
            String recipeText = (String) params.get("recipe");
            String goal = (String) params.get("goal");

            log.info("食谱优化请求，参数：{}, foodName：{}, goal：{}", params.keySet(), foodName, goal);

            String inputText = (foodName != null && !foodName.trim().isEmpty()) ? foodName : recipeText;

            if (inputText == null || inputText.trim().isEmpty()) {
                return ResponseResult.fail("400", "食谱内容不能为空");
            }

            log.info("食谱优化请求，目标：{}，输入：{}", goal, inputText.length() > 100 ? inputText.substring(0, 100) + "..." : inputText);
            Map<String, Object> result = zhipuAIService.optimizeRecipe(inputText);

            // 检查 service 层是否返回了错误
            if (Boolean.TRUE.equals(result.get("error"))) {
                String errorMsg = (String) result.get("message");
                log.warn("食谱优化服务返回错误：{}", errorMsg);
                return ResponseResult.fail("500", errorMsg != null ? errorMsg : "食谱优化失败");
            }

            log.info("食谱优化成功，返回字段：{}", result.keySet());

            // 兼容桌面端：前端期望 data 直接是数组
            Object recipes = result.get("recipes");
            if (recipes != null) {
                return ResponseResult.success(recipes);
            }
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return ResponseResult.fail("500", "食谱优化失败：" + e.getMessage());
        }
    }

    // ==================== 食谱优化（SSE流式进度，桌面端专用） ====================

    /**
     * AI食谱优化 SSE 流式端点
     *
     * 路由: POST /v1/ai/recipe/stream
     * 事件格式（复用 SupervisorSSEController 格式）：
     * - 进度: {"message":"正在分析食谱内容...","progress":true}
     * - 结果: {"recipes":[...],"done":true}
     * - 错误: {"error":true,"message":"...","done":true}
     */
    @ApiOperation(value = "AI食谱优化（SSE流式进度）", notes = "实时推送优化进度，桌面端专用")
    @PostMapping(value = "/recipe/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recipeOptimizeStream(@RequestBody Map<String, Object> params) {
        String foodName = (String) params.get("foodName");
        String recipeText = (String) params.get("recipe");
        String inputText = (foodName != null && !foodName.trim().isEmpty()) ? foodName : recipeText;

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        if (inputText == null || inputText.trim().isEmpty()) {
            sendRecipeSseData(emitter, Map.of("error", true, "message", "食谱内容不能为空", "done", true));
            emitter.complete();
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> result = zhipuAIService.optimizeRecipeWithProgress(
                        inputText,
                        message -> sendRecipeSseEvent(emitter, message)
                );

                Object recipes = result.get("recipes");
                if (recipes != null && !Boolean.TRUE.equals(result.get("error"))) {
                    sendRecipeSseData(emitter, Map.of("recipes", recipes, "done", true));
                } else {
                    sendRecipeSseData(emitter, Map.of("error", true,
                            "message", result.getOrDefault("message", "优化失败"), "done", true));
                }
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE食谱优化失败", e);
                sendRecipeSseData(emitter, Map.of("error", true,
                        "message", "优化失败：" + e.getMessage(), "done", true));
                emitter.complete();
            }
        }, recipeExecutorService);

        emitter.onTimeout(() -> {
            log.warn("SSE食谱优化连接超时");
            emitter.complete();
        });
        emitter.onError(e -> {
            log.error("SSE食谱优化连接错误", e);
        });

        return emitter;
    }

    private void sendRecipeSseEvent(SseEmitter emitter, String message) {
        try {
            final String jsonData = Objects.requireNonNull(objectMapper.writeValueAsString(
                    Map.of("message", message, "progress", true)));
            emitter.send(SseEmitter.event().name("message").data(jsonData));
        } catch (java.io.IOException e) {
            log.debug("SSE进度事件发送失败: {}", e.getMessage());
        }
    }

    private void sendRecipeSseData(SseEmitter emitter, Object data) {
        try {
            final String jsonData = Objects.requireNonNull(objectMapper.writeValueAsString(data));
            emitter.send(SseEmitter.event().name("message").data(jsonData));
        } catch (java.io.IOException e) {
            log.debug("SSE数据发送失败: {}", e.getMessage());
        }
    }

    // ==================== 菜品描述生成 ====================

    /**
     * AI菜品描述生成接口
     *
     * 路由: /v1/ai/dish-description
     */
    @ApiOperation(value = "AI菜品描述生成", notes = "使用AI生成吸引人的菜品描述")
    @PostMapping("/dish-description")
    public ResponseResult<?> generateDishDescription(@RequestBody DishDescriptionRequestDTO request) {
        try {
            if (request.getName() == null || request.getName().isEmpty()) {
                return ResponseResult.fail("400", "菜品名称不能为空");
            }
            String description = dishDescriptionService.generateDescription(request);
            return ResponseResult.success(description);
        } catch (Exception e) {
            log.error("菜品描述生成失败", e);
            return ResponseResult.fail("500", "菜品描述生成失败：" + e.getMessage());
        }
    }

    // ==================== 健康检查 ====================

    /**
     * 健康检查接口
     *
     * 路由: /v1/ai/health
     */
    @ApiOperation(value = "健康检查", notes = "检查AI服务状态")
    @GetMapping("/health")
    public ResponseResult<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AI Assistant");
        health.put("version", "3.0.0");
        health.put("timestamp", System.currentTimeMillis());
        health.put("features", new String[]{
            "菜品识别",
            "食谱优化"
        });
        health.put("architecture", "L2→L1两层架构");
        health.put("chatEndpoint", "/agent/supervisor-sse/chat");

        return ResponseResult.success(health);
    }
}
