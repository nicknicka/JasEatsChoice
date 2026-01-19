package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天消息控制器
 */
@Api(tags = "聊天消息模块")
@RestController
@RequestMapping("/v1/chat")
public class ChatController {

    @Autowired
    private ChatMsgService chatMsgService;

    @Autowired
    private ChatSessionService chatSessionService;


    /**
     * 获取聊天记录（分页）
     * @param sessionId 会话ID，可以是：
     *                  1. 单聊：两个用户ID用"_"拼接，如"user1_user2"
     *                  2. 群聊：群组ID，如"group123"
     */
    @ApiOperation("获取聊天记录")
    @GetMapping("/{sessionId}/messages")
    public ResponseResult<?> getChatMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Page<ChatMsg> chatMsgPage = new Page<>(page, size);
        LambdaQueryWrapper<ChatMsg> queryWrapper = new LambdaQueryWrapper<>();

        // 判断是单聊还是群聊
        if (sessionId.contains("_")) {
            // 单聊：会话ID格式为 "fromId_toId" 或 "toId_fromId"
            String[] userIds = sessionId.split("_");
            queryWrapper.and(wrapper -> wrapper
                    .eq(ChatMsg::getFromId, userIds[0])
                    .eq(ChatMsg::getToId, userIds[1]))
                    .or(wrapper -> wrapper
                            .eq(ChatMsg::getFromId, userIds[1])
                            .eq(ChatMsg::getToId, userIds[0]));
        } else {
            // 群聊：会话ID就是群组ID，作为toId
            queryWrapper.eq(ChatMsg::getToId, sessionId);
        }

        // 按时间倒序排序(最新的在前)
        queryWrapper.orderByDesc(ChatMsg::getCreateTime);

        // 查询结果
        Page<ChatMsg> result = chatMsgService.page(chatMsgPage, queryWrapper);

        // 将消息按时间正序排列(旧的在前,方便前端显示)
        java.util.Collections.reverse(result.getRecords());

        // 返回符合前端期望的格式
        Map<String, Object> responseData = new java.util.HashMap<>();
        responseData.put("records", result.getRecords());
        responseData.put("total", result.getTotal());
        responseData.put("current", result.getCurrent());
        responseData.put("pages", result.getPages());
        responseData.put("size", result.getSize());

        return ResponseResult.success(responseData);
    }

    /**
     * 发送消息
     */
    @ApiOperation("发送消息")
    @PostMapping("/messages")
    public ResponseResult<?> sendMessage(@RequestBody ChatMsg chatMsg) {
        // 设置默认值
        chatMsg.setReadStatus(false);
        chatMsg.setCreateTime(LocalDateTime.now());

        // ⭐ 生成并设置 session_id
        String sessionId = ChatSessionIdGenerator.generateSessionId(
            chatMsg.getMsgType(),
            chatMsg.getFromId(),
            chatMsg.getToId()
        );
        chatMsg.setSessionId(sessionId);

        // 保存消息
        boolean success = chatMsgService.save(chatMsg);
        if (success) {
            // ========== 创建或更新会话记录 ==========

            // 1. 为发送者创建或更新会话
            createOrUpdateSessionForUser(
                chatMsg.getFromId(),
                chatMsg.getToId(),
                chatMsg.getMsgType(),
                chatMsg.getContent(),
                chatMsg.getCreateTime()
            );

            // 2. 为接收者创建或更新会话（如果是群聊，所有成员都会收到消息）
            if ("group".equals(chatMsg.getMsgType())) {
                // 群聊：更新群组会话
                updateSessionForReceiver(
                    chatMsg.getToId(),  // 群ID
                    chatMsg.getFromId(),  // 发送者ID
                    chatMsg.getMsgType(),
                    chatMsg.getContent(),
                    chatMsg.getCreateTime(),
                    true  // 是群聊
                );
            } else {
                // 单聊：为接收者创建会话
                createOrUpdateSessionForUser(
                    chatMsg.getToId(),
                    chatMsg.getFromId(),
                    chatMsg.getMsgType(),
                    chatMsg.getContent(),
                    chatMsg.getCreateTime()
                );
            }

            return ResponseResult.success(chatMsg);
        } else {
            return ResponseResult.fail("500", "消息发送失败");
        }
    }

    /**
     * 为用户创建或更新会话
     */
    private void createOrUpdateSessionForUser(
            String userId,
            String otherId,
            String msgType,
            String content,
            LocalDateTime messageTime) {

        // 判断会话类型
        String sessionType = "group".equals(msgType) ? "group" : "single";
        String sessionId = "group".equals(msgType) ? otherId : buildSessionId(userId, otherId);

        // 查找现有会话
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId);
        queryWrapper.eq(ChatSession::getSessionId, sessionId);

        ChatSession session = chatSessionService.getOne(queryWrapper);

        if (session == null) {
            // 创建新会话
            session = new ChatSession();
            session.setId(String.valueOf(System.currentTimeMillis()));
            session.setUserId(userId);
            session.setSessionId(sessionId);
            session.setSessionType(sessionType);
            session.setSessionName(getSessionName(sessionType, otherId));
            session.setAvatar("group".equals(sessionType) ? "👥" : "💬");
            session.setMemberCount("group".equals(sessionType) ? 0 : null);
            session.setUnreadCount(0);
            session.setPinned(false);
            session.setCreateTime(LocalDateTime.now());
        }

        // 更新会话信息
        session.setLastMessage(content);
        session.setLastMessageTime(messageTime);
        session.setUpdateTime(LocalDateTime.now());

        // 保存会话
        if (session.getId() == null || chatSessionService.getById(session.getId()) == null) {
            chatSessionService.save(session);
        } else {
            chatSessionService.updateById(session);
        }
    }

    /**
     * 为接收者更新会话（增加未读数）
     */
    private void updateSessionForReceiver(
            String receiverId,
            String senderId,
            String msgType,
            String content,
            LocalDateTime messageTime,
            boolean isGroup) {

        String sessionType = isGroup ? "group" : "single";
        String sessionId = isGroup ? receiverId : buildSessionId(receiverId, senderId);

        // 查找现有会话
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, receiverId);
        queryWrapper.eq(ChatSession::getSessionId, sessionId);

        ChatSession session = chatSessionService.getOne(queryWrapper);

        if (session == null) {
            // 创建新会话
            session = new ChatSession();
            session.setId(String.valueOf(System.currentTimeMillis()));
            session.setUserId(receiverId);
            session.setSessionId(sessionId);
            session.setSessionType(sessionType);
            session.setSessionName(getSessionName(sessionType, isGroup ? receiverId : senderId));
            session.setAvatar("group".equals(sessionType) ? "👥" : "💬");
            session.setMemberCount(isGroup ? 0 : null);
            session.setUnreadCount(1);  // 新会话，初始未读数为1
            session.setPinned(false);
            session.setCreateTime(LocalDateTime.now());
            session.setLastMessage(content);
            session.setLastMessageTime(messageTime);
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.save(session);
        } else {
            // 更新现有会话，增加未读数
            session.setLastMessage(content);
            session.setLastMessageTime(messageTime);
            session.setUnreadCount((session.getUnreadCount() != null ? session.getUnreadCount() : 0) + 1);
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.updateById(session);
        }
    }

    /**
     * 构建会话ID（单聊）
     */
    private String buildSessionId(String userId1, String userId2) {
        // 按字典序排列，确保唯一性
        java.util.List<String> ids = new java.util.ArrayList<>();
        ids.add(userId1);
        ids.add(userId2);
        java.util.Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    /**
     * 获取会话名称
     */
    private String getSessionName(String sessionType, String otherId) {
        if ("group".equals(sessionType)) {
            return "\u7fa4\u804a " + otherId;
        } else {
            return "\u7528\u6237 " + otherId;
        }
    }

    /**
     * 标记消息已读
     */
    @ApiOperation("标记消息已读")
    @PutMapping("/messages/{messageId}/read")
    public ResponseResult<?> markMessageAsRead(@PathVariable String messageId) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setId(messageId);
        chatMsg.setReadStatus(true);

        boolean success = chatMsgService.updateById(chatMsg);
        if (success) {
            return ResponseResult.success("消息标记已读成功");
        } else {
            return ResponseResult.fail("500", "消息标记已读失败");
        }
    }

    /**
     * 撤回消息
     */
    @ApiOperation("撤回消息")
    @PostMapping("/messages/{messageId}/recall")
    public ResponseResult<?> recallMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request) {

        // 获取消息
        ChatMsg chatMsg = chatMsgService.getById(messageId);
        if (chatMsg == null) {
            return ResponseResult.fail("404", "消息不存在");
        }

        // 验证用户身份（只有发送者才能撤回）
        String userId = request.get("userId");
        if (!chatMsg.getFromId().equals(userId)) {
            return ResponseResult.fail("403", "无权撤回此消息");
        }

        // 检查消息是否已过期（2分钟内可以撤回）
        LocalDateTime createTime = chatMsg.getCreateTime();
        LocalDateTime now = LocalDateTime.now();
        long minutesDiff = java.time.Duration.between(createTime, now).toMinutes();
        if (minutesDiff > 2) {
            return ResponseResult.fail("400", "消息已超过2分钟，无法撤回");
        }

        // 检查消息内容是否已被撤回
        if ("消息已撤回".equals(chatMsg.getContent())) {
            return ResponseResult.fail("400", "消息已被撤回");
        }

        // 更新消息内容为"消息已撤回"
        ChatMsg updateMsg = new ChatMsg();
        updateMsg.setId(messageId);
        updateMsg.setContent("消息已撤回");

        boolean success = chatMsgService.updateById(updateMsg);
        if (success) {
            return ResponseResult.success("消息撤回成功");
        } else {
            return ResponseResult.fail("500", "消息撤回失败");
        }
    }
}

