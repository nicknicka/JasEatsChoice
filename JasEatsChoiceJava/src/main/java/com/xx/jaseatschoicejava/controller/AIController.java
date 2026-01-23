package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import com.xx.jaseatschoicejava.util.FileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI能力控制器
 */
@RestController
@RequestMapping("/v1/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    @Resource
    private ZhipuAIService zhipuAIService;

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

            // 1. 上传图片到服务器
            String uploadDir = fileUploadConfig.getUploadPath() + "dish-recognition/";
            String fileName = FileUploadUtil.uploadImage(image, uploadDir, userId);
            String imageUrl = fileUploadConfig.getUrlPrefix() + "dish-recognition/" + fileName;

            log.info("图片上传成功，URL：{}", imageUrl);

            // 2. 调用AI识别服务
            Map<String, Object> result = zhipuAIService.recognizeDish(imageUrl);

            // 3. 添加图片URL到结果中
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
     * AI聊天接口（对接智谱AI）
     */
    @PostMapping("/chat")
    public ResponseResult<?> chat(@RequestBody Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        String message = (String) params.get("message");

        log.info("=== AI聊天请求开始 ===");
        log.info("用户消息: {}", message);
        log.info("消息长度: {} 字符", message != null ? message.length() : 0);

        try {
            // 获取对话历史（可选）
            List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
            int historyCount = history != null ? history.size() : 0;
            log.info("对话历史轮数: {}", historyCount);

            // 调用智谱AI服务
            log.info("开始调用智谱AI服务...");
            String response = zhipuAIService.chat(message, history);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("智谱AI响应成功");
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
     * AI营养分析接口（对接智谱AI）
     */
    @PostMapping("/nutrient")
    public ResponseResult<?> nutrient(@RequestBody Map<String, Object> params) {
        try {
            String foodName = (String) params.get("foodName");

            Map<String, Object> result = zhipuAIService.analyzeNutrition(foodName);
            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("营养分析失败", e);
            return ResponseResult.fail("500", "营养分析失败：" + e.getMessage());
        }
    }

    /**
     * AI食谱推荐接口（对接智谱AI）
     */
    @PostMapping("/recipe")
    public ResponseResult<?> recipe(@RequestBody Map<String, Object> params) {
        try {
            String foodName = (String) params.get("foodName");

            List<Map<String, Object>> recipes = zhipuAIService.recommendRecipe(foodName);
            return ResponseResult.success(recipes);

        } catch (Exception e) {
            log.error("食谱推荐失败", e);
            return ResponseResult.fail("500", "食谱推荐失败：" + e.getMessage());
        }
    }
}
