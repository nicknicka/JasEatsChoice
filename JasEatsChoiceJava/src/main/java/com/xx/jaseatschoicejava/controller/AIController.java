package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.NutritionAiAgent;
import com.xx.jaseatschoicejava.agent.RecommendationAiAgent;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

/**
 * AI能力控制器
 *
 * 注意：部分功能已迁移到 Agent 系统
 * - 营养分析 → NutritionAgent
 * - 智能推荐 → RecommendationAgent
 * - 对话聊天 → NutritionAgent
 *
 * @author Claude
 * @since 2026-03-22
 */
@RestController
@RequestMapping("/v1/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    @Resource
    private ZhipuAIService zhipuAIService;

    @Resource
    private NutritionAiAgent nutritionAiAgent;

    @Resource
    private RecommendationAiAgent recommendationAiAgent;

    @Resource
    private FileUploadConfig fileUploadConfig;

    /**
     * AI菜品识别
     * 注意：GLM-4-Flash不支持图片识别，需要GLM-4V
     * 当前返回模拟数据
     */
    @PostMapping(value = "/dish-recognize", consumes = "multipart/form-data")
    public ResponseResult<?> dishRecognize(@RequestParam("image") MultipartFile image,
                                          @RequestParam(value = "userId", required = false) String userId) {
        try {
            log.info("接收到菜品识别请求，文件名：{}, 大小：{}", image.getOriginalFilename(), image.getSize());

            // 1. 读取图片字节数据（必须在transferTo之前读取，因为它会消耗InputStream）
            byte[] imageBytes;
            try (java.io.InputStream is = image.getInputStream()) {
                imageBytes = is.readAllBytes();
            }

            // 2. 将图片转为Base64编码（用于AI识别）
            String base64Data = java.util.Base64.getEncoder().encodeToString(imageBytes);
            log.info("图片Base64编码长度：{} 字符", base64Data.length());

            // 3. 手动保存图片文件（使用已读取的字节数组，避免重复读取InputStream）
            String uploadDir = fileUploadConfig.getUploadPath() + "dish-recognition/";
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成文件名
            String originalFilename = image.getOriginalFilename();
            String suffix = ".jpg"; // 默认后缀
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = java.util.UUID.randomUUID().toString() + suffix;

            // 保存文件（使用已读取的字节数组）
            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
            java.nio.file.Files.write(filePath, imageBytes);

            // 4. 构建图片URL
            String imageUrl = fileUploadConfig.getServerUrl() + "/" + fileUploadConfig.getUrlPrefix() + "dish-recognition/" + fileName;
            log.info("图片上传成功，URL：{}", imageUrl);

            log.info("图片Base64编码长度：{} 字符", base64Data != null ? base64Data.length() : 0);

            // 3. 调用AI识别服务（使用Base64编码）
            Map<String, Object> result = zhipuAIService.recognizeDishWithBase64(base64Data);

            // 4. 检查识别结果是否有效
            if (result == null) {
                log.error("AI识别返回null结果");
                return ResponseResult.fail("500", "菜品识别失败：服务返回空结果");
            }

            // 5. 检查识别结果是否包含错误
            if (Boolean.TRUE.equals(result.get("error"))) {
                // AI识别失败，返回错误信息
                String errorMessage = (String) result.get("message");
                log.error("AI识别失败：{}", errorMessage);
                return ResponseResult.fail("500", errorMessage != null ? errorMessage : "菜品识别失败");
            }

            // 6. 添加图片URL到结果中
            result.put("imageUrl", imageUrl);

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("菜品识别失败", e);
            return ResponseResult.fail("500", "菜品识别失败：" + e.getMessage());
        }
    }

    /**
     * 兼容旧接口：通过JSON传图片URL识别（不推荐使用）
     */
    @PostMapping("/dish-recognize-url")
    public ResponseResult<?> dishRecognizeByUrl(@RequestBody Map<String, Object> params) {
        try {
            String imageUrl = (String) params.get("imageUrl");
            Map<String, Object> result = zhipuAIService.recognizeDish(imageUrl);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("菜品识别失败", e);
            return ResponseResult.fail("500", "菜品识别失败：" + e.getMessage());
        }
    }

    /**
     * AI食谱优化
     */
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> recipe = (Map<String, Object>) params.get("recipe");
            String recipeText = (String) recipe.get("text");

            Map<String, Object> result = zhipuAIService.optimizeRecipe(recipeText);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return ResponseResult.fail("500", "食谱优化失败：" + e.getMessage());
        }
    }

    /**
     * AI聊天接口（使用 NutritionAgent）
     *
     * 已迁移到 Agent 系统，具备自动 Tool 调用能力
     */
    @PostMapping("/chat")
    public ResponseResult<?> chat(@RequestBody Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        String message = (String) params.get("message");

        log.info("=== AI聊天请求开始 ===");
        log.info("用户消息: {}", message);
        log.info("消息长度: {} 字符", message != null ? message.length() : 0);

        try {
            // 调用 NutritionAgent（具备 Tool 调用能力）
            log.info("开始调用 NutritionAgent...");
            String response = nutritionAiAgent.chat(message, "anonymous");

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Agent响应成功");
            log.info("响应长度: {} 字符", response.length());
            log.info("请求耗时: {} ms", duration);
            log.info("=== AI聊天请求完成 ===");

            Map<String, Object> result = Map.of("content", response);
            return ResponseResult.success(result);

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.error("AI聊天失败，耗时: {} ms", duration, e);
            log.error("错误详情: {}", e.getMessage());
            return ResponseResult.fail("500", "AI聊天失败：" + e.getMessage());
        }
    }

    /**
     * AI营养分析接口（使用 NutritionAgent）
     *
     * 已迁移到 Agent 系统，LLM 自动调用营养分析工具
     */
    @PostMapping("/nutrient")
    public ResponseResult<?> nutrient(@RequestBody Map<String, Object> params) {
        try {
            String foodName = (String) params.get("foodName");

            // 调用 NutritionAgent，让 LLM 自动调用营养分析工具
            String prompt = String.format("请分析【%s】的营养成分", foodName);
            String response = nutritionAiAgent.chat(prompt, "anonymous");

            Map<String, Object> result = Map.of(
                "foodName", foodName,
                "analysis", response
            );
            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("营养分析失败", e);
            return ResponseResult.fail("500", "营养分析失败：" + e.getMessage());
        }
    }

    /**
     * AI食谱推荐接口（使用 RecommendationAgent）
     *
     * 已迁移到 Agent 系统，LLM 自动调用食谱推荐工具
     */
    @PostMapping("/recipe")
    public ResponseResult<?> recipe(@RequestBody Map<String, Object> params) {
        try {
            String foodName = (String) params.get("foodName");

            // 调用 RecommendationAgent，让 LLM 自动调用食谱推荐工具
            String prompt = String.format("请推荐【%s】的食谱做法", foodName);
            String response = recommendationAiAgent.chat(prompt, "anonymous");

            Map<String, Object> result = Map.of(
                "foodName", foodName,
                "recommendation", response
            );
            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("食谱推荐失败",     e);
            return ResponseResult.fail("500", "食谱推荐失败：" + e.getMessage());
        }
    }
}
