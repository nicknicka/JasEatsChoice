package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
                    Map<String, Object> sessionMap = Map.of(
                            "id", session.getSessionId(),
                            "type", session.getSessionType(),
                            "name", session.getSessionName(),
                            "avatar", session.getAvatar() != null ? session.getAvatar() : (session.getSessionType().equals("group") ? "👥" : "💬"),
                            "lastMessage", session.getLastMessage() != null ? session.getLastMessage() : "暂无消息",
                            "time", session.getLastMessageTime() != null ?
                                    session.getLastMessageTime().toString().substring(11, 16) : "",
                            "unreadCount", session.getUnreadCount() != null ? session.getUnreadCount() : 0,
                            "pinned", session.getPinned() != null ? session.getPinned() : false,
                            "memberCount", session.getMemberCount() != null ? session.getMemberCount() : 0
                    );
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
            session.setUnreadCount(0);
            session.setPinned(false);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.save(session);
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
            session.setPinned(!session.getPinned());
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.updateById(session);
            return ResponseResult.success(session.getPinned());
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
