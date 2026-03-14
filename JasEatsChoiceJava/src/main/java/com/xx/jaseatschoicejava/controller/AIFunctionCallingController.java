package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.ai.function.AiFunctionDefinitionsOptimized;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Function Calling 控制器
 * 提供AI助手对话接口，支持Function Calling功能
 *
 * @author Claude
 * @since 2026-03-14
 */
@Slf4j
@Api(tags = "AI助手")
@RestController
@RequestMapping("/v1/ai/assistant")
public class AIFunctionCallingController {

    @Resource
    private ZhipuAIService zhipuAIService;

    @Resource
    private AiFunctionDefinitionsOptimized functionDefinitions;

    /**
     * AI助手对话接口（支持Function Calling）
     *
     * @param params 请求参数
     * @return AI回复
     */
    @ApiOperation(value = "AI助手对话", notes = "支持智能搜索菜品、营养分析、订单管理等功能")
    @PostMapping("/chat")
    public ResponseResult<?> chat(
            @ApiParam(value = "请求参数", required = true)
            @RequestBody Map<String, Object> params) {

        try {
            // 1. 提取参数
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");

            // 2. 参数验证
            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("用户 {} 发送消息：{}", userId, message);

            // 3. 调用AI服务
            String response = zhipuAIService.chat(message, history);

            // 4. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("reply", response);
            result.put("userId", userId);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("AI助手对话失败", e);
            return ResponseResult.fail("500", "对话失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用的工具函数列表
     *
     * @return 工具函数列表
     */
    @ApiOperation(value = "获取工具函数列表", notes = "返回所有可用的AI工具函数定义")
    @GetMapping("/tools")
    public ResponseResult<?> listTools() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("tools", functionDefinitions.getToolFunctions());
            result.put("count", functionDefinitions.getToolFunctions().size());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("获取工具函数列表失败", e);
            return ResponseResult.fail("500", "获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取系统提示词
     *
     * @return 系统提示词
     */
    @ApiOperation(value = "获取系统提示词", notes = "返回AI助手的系统提示词配置")
    @GetMapping("/prompt")
    public ResponseResult<?> getSystemPrompt() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("primary", functionDefinitions.getPrimarySystemPrompt());
            result.put("recommendation", functionDefinitions.getRecommendationPrompt());
            result.put("nutrition", functionDefinitions.getNutritionPrompt());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("获取系统提示词失败", e);
            return ResponseResult.fail("500", "获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取菜品分类列表
     *
     * @return 菜品分类列表
     */
    @ApiOperation(value = "获取菜品分类", notes = "返回所有可用的菜品分类")
    @GetMapping("/categories")
    public ResponseResult<?> listCategories() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("categories", functionDefinitions.getDishCategories());
            result.put("count", functionDefinitions.getDishCategories().size());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("获取菜品分类失败", e);
            return ResponseResult.fail("500", "获取失败：" + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     *
     * @return 服务状态
     */
    @ApiOperation(value = "健康检查", notes = "检查AI助手服务是否正常")
    @GetMapping("/health")
    public ResponseResult<?> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "AI Assistant");
        result.put("version", "1.0.0");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
