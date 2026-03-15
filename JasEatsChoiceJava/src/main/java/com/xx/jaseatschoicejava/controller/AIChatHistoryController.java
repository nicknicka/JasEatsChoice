package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.entity.AIChatHistory;
import com.xx.jaseatschoicejava.service.AIChatHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI聊天历史控制器
 */
@Slf4j
@Api(tags = "AI聊天历史管理")
@RestController
@RequestMapping("/v1/ai/chat")
public class AIChatHistoryController {

    @Resource
    private AIChatHistoryService aiChatHistoryService;

    /**
     * 获取用户聊天历史
     * GET /v1/ai/chat/history
     * @param userId 用户ID
     * @return 聊天历史列表
     */
    @ApiOperation("获取用户聊天历史")
    @GetMapping("/history")
    public Map<String, Object> getChatHistory(@RequestParam String userId) {
        try {
            log.info("获取用户AI聊天历史: userId={}", userId);

            List<AIChatHistory> historyList = aiChatHistoryService.getUserChatHistory(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", historyList);

            return result;
        } catch (Exception e) {
            log.error("获取用户AI聊天历史失败: userId={}", userId, e);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
            result.put("data", null);

            return result;
        }
    }

    /**
     * 保存聊天消息
     * POST /v1/ai/chat/save
     * @param params { userId, sender, content }
     * @return 保存结果
     */
    @ApiOperation("保存聊天消息")
    @PostMapping("/save")
    public Map<String, Object> saveMessage(@RequestBody Map<String, Object> params) {
        try {
            String userId = (String) params.get("userId");
            String sender = (String) params.get("sender");
            String content = (String) params.get("content");
            String messageType = (String) params.get("messageType");
            String cardData = (String) params.get("cardData");

            log.info("保存AI聊天消息: userId={}, sender={}, messageType={}", userId, sender, messageType);

            aiChatHistoryService.saveMessage(userId, sender, content, messageType, cardData);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "保存成功");

            return result;
        } catch (Exception e) {
            log.error("保存AI聊天消息失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "保存失败：" + e.getMessage());

            return result;
        }
    }

    /**
     * 清空用户聊天记录
     * DELETE /v1/ai/chat/clear
     * @param userId 用户ID
     * @return 删除结果
     */
    @ApiOperation("清空用户聊天记录")
    @DeleteMapping("/clear")
    public Map<String, Object> clearChatHistory(@RequestParam String userId) {
        try {
            log.info("清空用户AI聊天记录: userId={}", userId);

            aiChatHistoryService.deleteUserChatHistory(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "清空成功");

            return result;
        } catch (Exception e) {
            log.error("清空用户AI聊天记录失败: userId={}", userId, e);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "清空失败：" + e.getMessage());

            return result;
        }
    }

    /**
     * 检查用户是否有聊天历史
     * GET /v1/ai/chat/has-history
     * @param userId 用户ID
     * @return 是否有历史记录
     */
    @ApiOperation("检查是否有聊天历史")
    @GetMapping("/has-history")
    public Map<String, Object> hasChatHistory(@RequestParam String userId) {
        try {
            log.info("检查用户AI聊天历史: userId={}", userId);

            boolean hasHistory = aiChatHistoryService.hasChatHistory(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "检查成功");
            result.put("data", hasHistory);

            return result;
        } catch (Exception e) {
            log.error("检查用户AI聊天历史失败: userId={}", userId, e);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "检查失败：" + e.getMessage());
            result.put("data", false);

            return result;
        }
    }
}
