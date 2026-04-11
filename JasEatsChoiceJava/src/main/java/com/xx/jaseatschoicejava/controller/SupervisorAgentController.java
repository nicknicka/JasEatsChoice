package com.xx.jaseatschoicejava.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xx.jaseatschoicejava.agent.service.SupervisorAgentFactory;
import com.xx.jaseatschoicejava.common.ResponseResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.annotation.Resource;

/**
 * SupervisorAgent 控制器（V2 架构）
 *
 * 提供 Supervisor + StreamingResponse 的同步接口（UniApp 端）
 *
 * 执行流程：
 * 1. 同步阶段：SupervisorAgent 协调 L1 专家 Agent 收集数据
 * 2. 流式阶段：StreamingResponseAgent 格式化输出（同步收集完整结果）
 *
 * @author Claude
 * @since 2026-03-25
 * @updated 2026-04-03 V2: 同步 Supervisor + StreamingResponse 格式化
 */
@Api(tags = "Supervisor监督代理接口")
@RestController
@RequestMapping("/agent/supervisor")
@CrossOrigin(originPatterns = "*", allowCredentials = "false")
public class SupervisorAgentController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorAgentController.class);

    @Resource
    private SupervisorAgentFactory supervisorAgentFactory;

    /**
     * 统一聊天接口（智能路由）
     */
    @ApiOperation("统一聊天接口（智能路由）")
    @PostMapping("/chat")
    public ResponseResult<String> chat(@RequestBody ChatRequest request) {
        log.info("收到SupervisorAgent聊天请求, 用户ID: {}, 消息: {}",
                request.getUserId(), truncate(request.getMessage(), 100));

        try {
            String result = processWithSupervisor(request.getMessage(), request.getUserId());
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("SupervisorAgent处理失败", e);
            return ResponseResult.fail("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * 带用户上下文的聊天接口
     */
    @ApiOperation("带用户上下文的聊天接口")
    @PostMapping("/chatWithContext")
    public ResponseResult<String> chatWithContext(@RequestBody ChatRequest request) {
        log.info("收到SupervisorAgent聊天请求（带上下文）, 用户ID: {}, 消息: {}",
                request.getUserId(), truncate(request.getMessage(), 100));

        try {
            String messageWithUser = String.format("[用户ID: %s] %s",
                    request.getUserId(), request.getMessage());
            String result = processWithSupervisor(messageWithUser, request.getUserId());
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("SupervisorAgent处理失败", e);
            return ResponseResult.fail("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * GET方式的快速聊天接口
     */
    @ApiOperation("GET方式快速聊天")
    @GetMapping("/chat")
    public ResponseResult<String> quickChat(
            @ApiParam("用户消息") @RequestParam String message,
            @ApiParam("用户ID") @RequestParam(required = false) String userId) {
        log.info("收到SupervisorAgent快速聊天请求, 用户ID: {}, 消息: {}",
                userId, truncate(message, 100));

        try {
            String effectiveUserId = (userId != null && !userId.isEmpty()) ? userId : "anonymous";
            String messageWithUser = (userId != null && !userId.isEmpty())
                    ? String.format("[用户ID: %s] %s", userId, message)
                    : message;
            String result = processWithSupervisor(messageWithUser, effectiveUserId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("SupervisorAgent处理失败", e);
            return ResponseResult.fail("500", "处理失败: " + e.getMessage());
        }
    }

    /**
    * 核心处理方法：Supervisor 同步执行后直接返回结果
     *
     * 阶段1：SupervisorAgent 协调 L1 专家 Agent（同步）
     */
    private String processWithSupervisor(String message, String userId) {
        // ===== 阶段1：同步 Supervisor 执行 =====
        log.info("[阶段1] SupervisorAgent开始处理: userId={}", userId);
        dev.langchain4j.agentic.supervisor.SupervisorAgent supervisorAgent =
                supervisorAgentFactory.createWithListener(null, userId);
        String supervisorResult = supervisorAgent.invoke(message);

        // 清理 LangChain4j 调试信息
        String cleanedResult = supervisorAgentFactory.cleanDebugInfo(supervisorResult);
        log.info("[阶段1] SupervisorAgent完成，结果长度: {} 字符", cleanedResult.length());

        log.info("[阶段2] 直接返回阶段1结果: userId={}, 结果长度: {}", userId, cleanedResult.length());
        return cleanedResult;
    }

    /**
     * 聊天请求DTO
     */
    public static class ChatRequest {
        private String message;
        private String userId;
        private String sessionId;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    /**
     * 截断过长的文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "null";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
