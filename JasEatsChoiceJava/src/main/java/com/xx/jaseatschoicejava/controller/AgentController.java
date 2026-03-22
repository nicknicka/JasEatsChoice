package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.service.IntelligentAdvisorAgent;
import com.xx.jaseatschoicejava.agent.service.NutritionAgent;
import com.xx.jaseatschoicejava.agent.service.OrderAssistantAgent;
import com.xx.jaseatschoicejava.agent.service.RecommendationAgent;
import com.xx.jaseatschoicejava.common.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent控制器
 * 提供LangChain4j Agent的REST API接口
 *
 * @author Claude
 * @since 2026-03-22
 */
@Api(tags = "AI Agent接口")
@RestController
@RequestMapping("/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    @Resource
    private IntelligentAdvisorAgent intelligentAdvisorAgent;

    @Resource
    private NutritionAgent nutritionAgent;

    @Resource
    private RecommendationAgent recommendationAgent;

    @Resource
    private OrderAssistantAgent orderAssistantAgent;

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
            String userId = (String) params.getOrDefault("userId", "anonymous");

            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("收到营养Agent请求 [用户:{}]：{}", userId, message);

            // 调用Agent
            String response = nutritionAgent.chat(message, userId);

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
     * 测试推荐Agent
     *
     * @param params 请求参数
     * @return Agent回复
     */
    @ApiOperation(value = "测试推荐Agent", notes = "发送消息给推荐Agent")
    @PostMapping("/recommendation/chat")
    public ResponseResult<?> recommendationChat(@RequestBody Map<String, Object> params) {
        try {
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");

            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("收到推荐Agent请求 [用户:{}]：{}", userId, message);

            String response = recommendationAgent.chat(message, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("agent", "recommendation");
            result.put("request", message);
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("推荐Agent调用失败", e);
            return ResponseResult.fail("500", "调用失败：" + e.getMessage());
        }
    }

    /**
     * 测试订单助手Agent
     *
     * @param params 请求参数
     * @return Agent回复
     */
    @ApiOperation(value = "测试订单助手Agent", notes = "发送消息给订单助手Agent")
    @PostMapping("/order/chat")
    public ResponseResult<?> orderChat(@RequestBody Map<String, Object> params) {
        try {
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");

            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("收到订单助手Agent请求 [用户:{}]：{}", userId, message);

            String response = orderAssistantAgent.chat(message, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("agent", "order");
            result.put("request", message);
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("订单助手Agent调用失败", e);
            return ResponseResult.fail("500", "调用失败：" + e.getMessage());
        }
    }

    /**
     * 智能顾问对话（主入口）
     *
     * @param params 请求参数
     * @return Agent回复
     */
    @ApiOperation(value = "智能顾问对话", notes = "智能顾问会自动路由到合适的Agent")
    @PostMapping("/chat")
    public ResponseResult<?> chat(@RequestBody Map<String, Object> params) {
        try {
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");

            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("收到智能顾问请求 [用户:{}]：{}", userId, message);

            // 调用智能顾问
            String response = intelligentAdvisorAgent.chat(message, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("agent", "intelligent-advisor");
            result.put("request", message);
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("智能顾问调用失败", e);
            return ResponseResult.fail("500", "调用失败：" + e.getMessage());
        }
    }

    /**
     * 获取对话历史
     *
     * @param userId 用户ID
     * @param agent Agent类型
     * @return 对话历史
     */
    @ApiOperation(value = "获取对话历史", notes = "获取指定Agent的对话历史")
    @GetMapping("/history/{userId}")
    public ResponseResult<?> getHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "advisor") String agent) {

        try {
            List<String> history;

            switch (agent.toLowerCase()) {
                case "nutrition":
                    history = nutritionAgent.getChatHistory(userId);
                    break;
                case "recommendation":
                    history = recommendationAgent.getChatHistory(userId);
                    break;
                case "order":
                    history = orderAssistantAgent.getChatHistory(userId);
                    break;
                case "all":
                    Map<String, List<String>> allHistory = intelligentAdvisorAgent.getAllAgentHistory(userId);
                    return ResponseResult.success(allHistory);
                case "advisor":
                default:
                    history = intelligentAdvisorAgent.getChatHistory(userId);
                    break;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("agent", agent);
            result.put("userId", userId);
            result.put("history", history);
            result.put("count", history.size());

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            return ResponseResult.fail("500", "获取失败：" + e.getMessage());
        }
    }

    /**
     * 清除对话上下文
     *
     * @param userId 用户ID
     * @param agent Agent类型
     * @return 操作结果（包含欢迎消息）
     */
    @ApiOperation(value = "清除对话上下文", notes = "清除指定Agent的对话记忆并返回欢迎消息")
    @DeleteMapping("/context/{userId}")
    public ResponseResult<?> clearContext(
            @PathVariable String userId,
            @RequestParam(defaultValue = "all") String agent) {

        try {
            switch (agent.toLowerCase()) {
                case "nutrition":
                    nutritionAgent.clearMemory(userId);
                    break;
                case "recommendation":
                    recommendationAgent.clearMemory(userId);
                    break;
                case "order":
                    orderAssistantAgent.clearMemory(userId);
                    break;
                case "advisor":
                    intelligentAdvisorAgent.clearMemory(userId);
                    break;
                case "all":
                default:
                    intelligentAdvisorAgent.clearMemory(userId);
                    break;
            }

            // 获取欢迎消息
            String welcomeMessage = intelligentAdvisorAgent.getWelcomeMessage();

            Map<String, Object> result = new HashMap<>();
            result.put("message", "对话上下文已清除");
            result.put("welcomeMessage", welcomeMessage);

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("清除对话上下文失败", e);
            return ResponseResult.fail("500", "清除失败：" + e.getMessage());
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
                "recommendation", "available",
                "order", "available",
                "advisor", "available"
        ));
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }

    /**
     * 获取Agent列表
     *
     * @return 可用的Agent列表
     */
    @ApiOperation(value = "获取Agent列表", notes = "返回所有可用的Agent及其能力")
    @GetMapping("/list")
    public ResponseResult<?> listAgents() {
        Map<String, Object> advisor = Map.of(
                "name", "智能顾问",
                "code", "advisor",
                "status", "available",
                "description", "总协调器，自动路由到合适的专家Agent",
                "capabilities", new String[]{
                        "意图识别",
                        "Agent路由",
                        "多Agent协作",
                        "对话管理"
                }
        );

        Map<String, Object> nutrition = Map.of(
                "name", "营养师",
                "code", "nutrition",
                "status", "available",
                "description", "专业营养分析和健康饮食建议",
                "capabilities", new String[]{
                        "营养成分分析",
                        "卡路里计算",
                        "每日热量需求",
                        "饮食健康评估"
                }
        );

        Map<String, Object> recommendation = Map.of(
                "name", "推荐师",
                "code", "recommendation",
                "status", "available",
                "description", "个性化美食推荐和搜索",
                "capabilities", new String[]{
                        "今日推荐",
                        "按卡路里推荐",
                        "菜品搜索",
                        "热门菜品",
                        "多人套餐推荐"
                }
        );

        Map<String, Object> order = Map.of(
                "name", "订餐助手",
                "code", "order",
                "status", "available",
                "description", "智能下单和订单管理",
                "capabilities", new String[]{
                        "智能下单",
                        "创建订单",
                        "查询订单",
                        "取消订单",
                        "预估送达时间"
                }
        );

        Map<String, Object> result = new HashMap<>();
        result.put("agents", new Object[]{advisor, nutrition, recommendation, order});
        result.put("count", 4);
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
