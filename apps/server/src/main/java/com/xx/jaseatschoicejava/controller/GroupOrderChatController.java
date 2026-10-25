package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.service.GroupOrderChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 群订单专属会话控制器
 */
@RestController
@RequestMapping("/v1/group-order-chat")
@RequiredArgsConstructor
public class GroupOrderChatController {

    private static final Logger logger = LoggerFactory.getLogger(GroupOrderChatController.class);

    private final GroupOrderChatService groupOrderChatService;

    /**
     * 创建群订单专属会话
     * 通常在商家接单后自动调用
     */
    @PostMapping("/sessions/{groupOrderId}")
    public ResponseResult<?> createGroupOrderSession(@PathVariable String groupOrderId,
                                              @RequestParam String groupId,
                                              @RequestParam String merchantId) {
        try {
            ChatSession session = groupOrderChatService.createGroupOrderSession(groupOrderId, groupId, merchantId);
            logger.info("群订单专属会话创建成功 - sessionId: {}", session.getSessionId());
            return ResponseResult.success(session);
        } catch (Exception e) {
            logger.error("创建群订单专属会话失败 - groupOrderId: {}", groupOrderId, e);
            return ResponseResult.fail("500", "创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 同步消息到群订单会话
     * 商家端点击"同步到群"按钮时调用
     */
    @PostMapping("/sync")
    public ResponseResult<?> syncMessageToGroup(@RequestBody Map<String, Object> request) {
        try {
            String merchantId = request.get("merchantId").toString();
            String groupOrderId = request.get("groupOrderId").toString();
            String message = request.get("message") != null ? request.get("message").toString() : "";
            String messageType = request.get("messageType") != null ? request.get("messageType").toString() : "订单通知";

            String syncedMessage = groupOrderChatService.syncMessageToGroup(
                    merchantId, groupOrderId, message, messageType);

            if (syncedMessage != null) {
                logger.info("消息同步成功 - groupOrderId: {}, message: {}", groupOrderId, syncedMessage);
                return ResponseResult.success(Map.of(
                        "syncedMessage", syncedMessage,
                        "syncTime", System.currentTimeMillis()
                ));
            }
            return ResponseResult.fail("404", "群订单专属会话不存在");
        } catch (Exception e) {
            logger.error("同步消息失败 - groupOrderId: {}", request.get("groupOrderId"), e);
            return ResponseResult.fail("500", "同步消息失败：" + e.getMessage());
        }
    }

    /**
     * 获取群订单专属会话
     */
    @GetMapping("/sessions/{groupOrderId}")
    public ResponseResult<?> getGroupOrderSession(@PathVariable String groupOrderId) {
        try {
            ChatSession session = groupOrderChatService.getGroupOrderSession(groupOrderId);
            if (session != null) {
                return ResponseResult.success(session);
            }
            return ResponseResult.fail("404", "群订单专属会话不存在");
        } catch (Exception e) {
            logger.error("获取群订单专属会话失败 - groupOrderId: {}", groupOrderId, e);
            return ResponseResult.fail("500", "获取会话失败：" + e.getMessage());
        }
    }

    /**
     * 检查群订单是否有专属会话
     */
    @GetMapping("/sessions/{groupOrderId}/exists")
    public ResponseResult<?> hasGroupOrderSession(@PathVariable String groupOrderId) {
        try {
            boolean exists = groupOrderChatService.hasGroupOrderSession(groupOrderId);
            return ResponseResult.success(Map.of("exists", exists));
        } catch (Exception e) {
            logger.error("检查群订单会话失败 - groupOrderId: {}", groupOrderId, e);
            return ResponseResult.fail("500", "检查会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取待同步的消息列表
     * 商家端查看待同步消息时调用
     */
    @GetMapping("/pending-sync/{merchantId}")
    public ResponseResult<?> getPendingSyncMessages(@PathVariable String merchantId,
                                             @RequestParam(required = false) String sessionType) {
        try {
            List<Map<String, Object>> messages = groupOrderChatService.getPendingSyncMessages(merchantId, sessionType);
            return ResponseResult.success(messages);
        } catch (Exception e) {
            logger.error("获取待同步消息失败 - merchantId: {}", merchantId, e);
            return ResponseResult.fail("500", "获取消息失败：" + e.getMessage());
        }
    }

    /**
     * 标记消息为已同步
     * 同步成功后调用
     */
    @PostMapping("/messages/{messageId}/synced")
    public ResponseResult<?> markMessageAsSynced(@PathVariable String messageId) {
        try {
            boolean success = groupOrderChatService.markMessageAsSynced(messageId);
            if (success) {
                return ResponseResult.success(Map.of("messageId", messageId, "synced", true));
            }
            return ResponseResult.fail("404", "消息不存在");
        } catch (Exception e) {
            logger.error("标记消息已同步失败 - messageId: {}", messageId, e);
            return ResponseResult.fail("500", "标记失败：" + e.getMessage());
        }
    }

    /**
     * 归档群订单专属会话
     * 订单完成后调用
     */
    @PostMapping("/sessions/{groupOrderId}/archive")
    public ResponseResult<?> archiveGroupOrderSession(@PathVariable String groupOrderId) {
        try {
            boolean success = groupOrderChatService.archiveGroupOrderSession(groupOrderId);
            if (success) {
                logger.info("群订单专属会话归档成功 - groupOrderId: {}", groupOrderId);
                return ResponseResult.success(Map.of("groupOrderId", groupOrderId, "archived", true));
            }
            return ResponseResult.fail("404", "群订单专属会话不存在");
        } catch (Exception e) {
            logger.error("归档群订单专属会话失败 - groupOrderId: {}", groupOrderId, e);
            return ResponseResult.fail("500", "归档失败：" + e.getMessage());
        }
    }
}
