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
 * Agent测试控制器
 * 用于测试LangChain4j Agent功能
 *
 * @author Claude
 * @since 2026-03-22
 */
@Slf4j
@Api(tags = "Agent测试")
@RestController
@RequestMapping("/v1/agent")
public class AgentController {

    @Resource
    private NutritionAgent nutritionAgent;

    /**
     * 测试营养分析Agent
     *
     * @param params 请求参数
     * @return Agent回复
     */
    @ApiOperation(value = "测试营养Agent", notes = "发送消息给营养分析Agent")
    @PostMapping("/nutrition/chat")
    public ResponseResult<?> nutritionChat(@RequestBody Map<String, Object> params) {
        try {
            String message = (String) params.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("收到营养Agent请求：{}", message);

            // 调用Agent
            String response = nutritionAgent.chat(message);

            log.info("营养Agent回复：{}", response);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("agent", "nutrition");
            result.put("request", message);
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("营养Agent调用失败", e);
            return ResponseResult.fail("500", "调用失败：" + e.getMessage());
        }
    }

    /**
     * Agent健康检查
     *
     * @return 服务状态
     */
    @ApiOperation(value = "Agent健康检查", notes = "检查Agent服务是否正常")
    @GetMapping("/health")
    public ResponseResult<?> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "Agent");
        result.put("agents", Map.of(
                "nutrition", "available",
                "recommendation", "pending",
                "order", "pending",
                "advisor", "pending"
        ));
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }

    /**
     * 获取Agent列表
     *
     * @return 可用的Agent列表
     */
    @ApiOperation(value = "获取Agent列表", notes = "返回所有可用的Agent")
    @GetMapping("/list")
    public ResponseResult<?> listAgents() {
        Map<String, Object> nutritionAgent = Map.of(
                "name", "营养分析Agent",
                "status", "available",
                "description", "分析食物营养成分，提供营养建议",
                "capabilities", new String[]{
                        "分析单一食物营养",
                        "批量分析多个食物",
                        "计算每日热量需求",
                        "提供健康建议"
                }
        );

        Map<String, Object> result = new HashMap<>();
        result.put("agents", new Object[]{nutritionAgent});
        result.put("count", 1);
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
