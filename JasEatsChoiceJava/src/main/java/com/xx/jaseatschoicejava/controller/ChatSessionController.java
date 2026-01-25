package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天会话控制器
 */
@Slf4j
@Api(tags = "聊天会话管理")
@RestController
@RequestMapping("/v1/chat")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMsgService chatMsgService;

    /**
     * 获取用户的所有会话列表
     */
    @ApiOperation("获取用户会话列表")
    @GetMapping("/users/{userId}/chat-sessions")
    public ResponseResult<?> getUserChatSessions(@PathVariable String userId) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId);
        queryWrapper.orderByDesc(ChatSession::getPinned); // 置顶的在前
        queryWrapper.orderByDesc(ChatSession::getLastMessageTime); // 按最后消息时间降序

        log.info("获取用户会话列表: userId={}", userId);
        List<ChatSession> sessions = chatSessionService.list(queryWrapper);
        log.info("获取用户会话列表: {}", sessions);
        // 转换为前端需要的格式
        List<Map<String, Object>> result = sessions.stream()
                .map(session -> {
                    Map<String, Object> sessionMap = new HashMap<>();
                    sessionMap.put("id", session.getSessionId());
                    sessionMap.put("type", session.getSessionType());
                    sessionMap.put("name", session.getSessionName());
                    sessionMap.put("avatar", session.getAvatar() != null ? session.getAvatar() : (session.getSessionType().equals("group") ? "👥" : "💬"));
                    sessionMap.put("lastMessage", session.getLastMessage() != null ? session.getLastMessage() : "暂无消息");
                    sessionMap.put("time", session.getLastMessageTime() != null ?
                            session.getLastMessageTime().toString().substring(11, 16) : "");
                    sessionMap.put("unreadCount", session.getUnreadCount() != null ? session.getUnreadCount() : 0);
                    sessionMap.put("pinned", session.getPinned() != null ? session.getPinned() == 1 : false);
                    sessionMap.put("memberCount", session.getMemberCount() != null ? session.getMemberCount() : 0);
                    // ⭐ 添加 groupId（仅群聊会话有值）
                    sessionMap.put("groupId", session.getGroupId());
                    return sessionMap;
                })
                .collect(Collectors.toList());

        return ResponseResult.success(result);
    }

    /**
     * 创建或更新会话
     */
    @ApiOperation("创建或更新会话")
    @PostMapping("/sessions")
    public ResponseResult<?> createOrUpdateSession(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        String sessionId = (String) params.get("sessionId");
        String sessionType = (String) params.get("sessionType");
        String sessionName = (String) params.get("sessionName");
        String avatar = (String) params.get("avatar");
        Integer memberCount = params.get("memberCount") != null ?
                (Integer) params.get("memberCount") : 0;

        String groupId = (String) params.get("groupId"); // ⭐ 直接从请求参数中获取groupId

        // ⭐ 对群聊的sessionId进行转换
        if ("group".equals(sessionType)) {
            // 如果没有传groupId，且sessionId是G开头的，从sessionId中提取
            if (groupId == null && sessionId != null && sessionId.startsWith("G")) {
                groupId = sessionId;
            }

            // 如果 sessionId 不是 S 开头，需要转换
            if (sessionId != null && !sessionId.startsWith("S")) {
                sessionId = ChatSessionIdGenerator.getGroupChatSessionId(sessionId);
            }
            // 如果已经是 S 开头，直接使用
        } else {
            // 单聊：使用IdGenerator生成sessionId
            sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(
                userId,
                sessionId
            );
        }

        // 查找是否已存在该会话
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId);
        queryWrapper.eq(ChatSession::getSessionId, sessionId);

        ChatSession session = chatSessionService.getOne(queryWrapper);

        if (session == null) {
            // 创建新会话
            session = new ChatSession();
            session.setUserId(userId);
            session.setSessionId(sessionId);
            session.setSessionType(sessionType);
            session.setSessionName(sessionName);
            session.setAvatar(avatar);
            session.setMemberCount(memberCount);
            session.setGroupId(groupId); // ⭐ 保存 groupId（仅群聊有值）
            session.setUnreadCount(0);
            session.setPinned(0);  // 0-未置顶
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());

            // 使用 saveOrUpdate 避免并发插入导致的唯一键冲突
            try {
                chatSessionService.saveOrUpdate(session);
            } catch (Exception e) {
                // 如果保存失败，可能是并发导致的唯一键冲突，重新查询并更新
                log.warn("保存会话失败，尝试重新查询并更新: userId={}, sessionId={}, error={}",
                        userId, sessionId, e.getMessage());
                ChatSession existingSession = chatSessionService.getOne(queryWrapper);
                if (existingSession != null) {
                    existingSession.setSessionName(sessionName);
                    existingSession.setAvatar(avatar);
                    existingSession.setMemberCount(memberCount);
                    existingSession.setUpdateTime(LocalDateTime.now());
                    chatSessionService.updateById(existingSession);
                    return ResponseResult.success(existingSession);
                } else {
                    throw e;
                }
            }
        } else {
            // 更新会话信息
            session.setSessionName(sessionName);
            session.setAvatar(avatar);
            session.setMemberCount(memberCount);
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.updateById(session);
        }

        return ResponseResult.success(session);
    }

    /**
     * 更新会话最后消息
     */
    @ApiOperation("更新会话最后消息")
    @PostMapping("/sessions/{sessionId}/last-message")
    public ResponseResult<?> updateLastMessage(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        String content = (String) params.get("content");
        LocalDateTime messageTime = LocalDateTime.now();

        // 更新发送者的会话
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getUserId, userId);
        updateWrapper.eq(ChatSession::getSessionId, sessionId);
        updateWrapper.set(ChatSession::getLastMessage, content);
        updateWrapper.set(ChatSession::getLastMessageTime, messageTime);
        updateWrapper.set(ChatSession::getUpdateTime, LocalDateTime.now());
        chatSessionService.update(updateWrapper);

        return ResponseResult.success("更新成功");
    }

    /**
     * 增加会话未读消息数
     */
    @ApiOperation("增加未读消息数")
    @PostMapping("/sessions/{sessionId}/unread-increment")
    public ResponseResult<?> incrementUnreadCount(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");

        ChatSession session = chatSessionService.getOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getSessionId, sessionId)
        );

        if (session != null) {
            session.setUnreadCount((session.getUnreadCount() != null ? session.getUnreadCount() : 0) + 1);
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.updateById(session);
        }

        return ResponseResult.success("更新成功");
    }

    /**
     * 清除会话未读消息数
     */
    @ApiOperation("清除未读消息数")
    @PostMapping("/sessions/{sessionId}/unread-clear")
    public ResponseResult<?> clearUnreadCount(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");

        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getUserId, userId);
        updateWrapper.eq(ChatSession::getSessionId, sessionId);
        updateWrapper.set(ChatSession::getUnreadCount, 0);
        updateWrapper.set(ChatSession::getUpdateTime, LocalDateTime.now());
        chatSessionService.update(updateWrapper);

        return ResponseResult.success("清除成功");
    }

    /**
     * 切换会话置顶状态
     */
    @ApiOperation("切换置顶状态")
    @PostMapping("/sessions/{sessionId}/toggle-pin")
    public ResponseResult<?> togglePin(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");

        ChatSession session = chatSessionService.getOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getSessionId, sessionId)
        );

        if (session != null) {
            // 切换置顶状态：0变1，1变0
            int newPinned = (session.getPinned() == null || session.getPinned() == 0) ? 1 : 0;
            session.setPinned(newPinned);
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.updateById(session);
            return ResponseResult.success(newPinned == 1);
        }

        return ResponseResult.fail("404", "会话不存在");
    }

    /**
     * 删除会话
     */
    @ApiOperation("删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseResult<?> deleteSession(
            @PathVariable String sessionId,
            @RequestParam String userId) {

        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId);
        queryWrapper.eq(ChatSession::getSessionId, sessionId);

        boolean deleted = chatSessionService.remove(queryWrapper);

        if (deleted) {
            return ResponseResult.success("删除成功");
        }

        return ResponseResult.fail("404", "会话不存在");
    }
}
