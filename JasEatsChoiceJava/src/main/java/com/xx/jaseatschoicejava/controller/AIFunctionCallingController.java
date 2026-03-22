package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.service.NutritionAgent;
import com.xx.jaseatschoicejava.common.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * AI Function Calling 控制器（使用LangChain4j）
 * 提供AI助手对话接口，支持Agent功能
 *
 * @author Claude
 * @since 2026-03-22
 */
@Slf4j
@Api(tags = "AI助手（LangChain4j）")
@RestController
@RequestMapping("/v1/ai/assistant")
public class AIFunctionCallingController {

    @Resource
    private NutritionAgent nutritionAgent;

    /**
     * AI助手对话接口（使用LangChain4j Agent）
     *
     * @param params 请求参数
     * @return AI回复
     */
    @ApiOperation(value = "AI助手对话", notes = "使用LangChain4j Agent，支持营养分析、卡路里计算等功能")
    @PostMapping("/chat")
    public ResponseResult<?> chat(
            @RequestBody Map<String, Object> params) {

        try {
            // 1. 提取参数
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");

            // 2. 参数验证
            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("用户 {} 发送消息：{}", userId, message);

            // 3. 调用LangChain4j Agent
            String response = nutritionAgent.chat(message, userId);

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
     * 获取Agent信息
     *
     * @return Agent信息
     */
    @ApiOperation(value = "获取Agent信息", notes = "返回当前可用的Agent列表和信息")
    @GetMapping("/agents")
    public ResponseResult<?> listAgents() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("agents", Map.of(
                "nutrition", "营养分析Agent（可用）",
                "recommendation", "智能推荐Agent（开发中）",
                "order", "订单助手Agent（开发中）",
                "advisor", "智能顾问Agent（开发中）"
            ));
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("获取Agent信息失败", e);
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
        result.put("service", "AI Assistant (LangChain4j)");
        result.put("version", "2.0.0");
        result.put("framework", "LangChain4j 0.29.1");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
