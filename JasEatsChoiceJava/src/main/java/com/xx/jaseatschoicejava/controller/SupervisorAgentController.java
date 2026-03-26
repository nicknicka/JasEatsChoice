package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * SupervisorAgent 控制器
 *
 * 提供L3监督代理的统一接口，智能调度L2领域Agent
 *
 * @author Claude
 * @since 2026-03-25
 */
@Api(tags = "L3监督代理接口")
@RestController
@RequestMapping("/agent/supervisor")
@CrossOrigin(originPatterns = "*", allowCredentials = "false")
public class SupervisorAgentController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorAgentController.class);

    @Resource
    private SupervisorAgent supervisorAgent;

    @Resource
    private com.xx.jaseatschoicejava.agent.service.SupervisorAgentFactory supervisorAgentFactory;

    /**
     * 统一聊天接口
     *
     * SupervisorAgent会自动分析用户问题，智能路由到合适的L2 Agent
     *
     * @param request 聊天请求
     * @return Agent响应
     */
    @ApiOperation("统一聊天接口（智能路由）")
    @PostMapping("/chat")
    public ResponseResult<String> chat(@RequestBody ChatRequest request) {
        log.info("收到SupervisorAgent聊天请求, 用户ID: {}, 消息: {}",
                request.getUserId(), truncate(request.getMessage(), 100));

        try {
            String response = supervisorAgent.invoke(request.getMessage());

            // 渲染为卡片格式
            String renderedResponse = supervisorAgentFactory.renderCards(response);

            log.info("SupervisorAgent响应成功, 长度: {}", renderedResponse.length());
            return ResponseResult.success(renderedResponse);
        } catch (Exception e) {
            log.error("SupervisorAgent处理失败", e);
            return ResponseResult.fail("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * 带用户上下文的聊天接口
     *
     * 使用用户ID进行个性化查询和推荐
     *
     * @param request 聊天请求（包含用户ID）
     * @return Agent响应
     */
    @ApiOperation("带用户上下文的聊天接口")
    @PostMapping("/chatWithContext")
    public ResponseResult<String> chatWithContext(@RequestBody ChatRequest request) {
        log.info("收到SupervisorAgent聊天请求（带上下文）, 用户ID: {}, 消息: {}",
                request.getUserId(), truncate(request.getMessage(), 100));

        try {
            // 将用户ID拼接到消息中，方便Supervisor处理
            String messageWithUser = String.format("[用户ID: %s] %s",
                    request.getUserId(), request.getMessage());
            String response = supervisorAgent.invoke(messageWithUser);

            // 渲染为卡片格式
            String renderedResponse = supervisorAgentFactory.renderCards(response);

            log.info("SupervisorAgent响应成功, 长度: {}", renderedResponse.length());
            return ResponseResult.success(renderedResponse);
        } catch (Exception e) {
            log.error("SupervisorAgent处理失败", e);
            return ResponseResult.fail("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * GET方式的快速聊天接口
     *
     * @param message 用户消息
     * @param userId 用户ID（可选）
     * @return Agent响应
     */
    @ApiOperation("GET方式快速聊天")
    @GetMapping("/chat")
    public ResponseResult<String> quickChat(
            @ApiParam("用户消息") @RequestParam String message,
            @ApiParam("用户ID") @RequestParam(required = false) String userId) {
        log.info("收到SupervisorAgent快速聊天请求, 用户ID: {}, 消息: {}",
                userId, truncate(message, 100));

        try {
            // 将用户ID拼接到消息中（如果有）
            String messageWithUser = (userId != null && !userId.isEmpty())
                    ? String.format("[用户ID: %s] %s", userId, message)
                    : message;
            String response = supervisorAgent.invoke(messageWithUser);

            // 渲染为卡片格式
            String renderedResponse = supervisorAgentFactory.renderCards(response);

            log.info("SupervisorAgent响应成功, 长度: {}", renderedResponse.length());
            return ResponseResult.success(renderedResponse);
        } catch (Exception e) {
            log.error("SupervisorAgent处理失败", e);
            return ResponseResult.fail("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * 聊天请求DTO
     */
    public static class ChatRequest {
        private String message;
        private String userId;
        private String sessionId;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    /**
     * 截断过长的文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "null";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
