package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息控制器
 */
@Api(tags = "聊天消息模块")
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    @Autowired
    private ChatMsgService chatMsgService;

    /**
     * 获取聊天会话列表
     */
    @ApiOperation("获取聊天会话列表")
    @GetMapping("/users/{userId}/chat-sessions")
    public ResponseResult<?> getChatSessions(@PathVariable String userId) {
        // 会话列表是与该用户有过聊天记录的所有用户/群组的列表
        // 查询条件：fromId或toId等于当前用户ID，然后按时间倒序取最新的消息
        LambdaQueryWrapper<ChatMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.or(wrapper -> wrapper.eq(ChatMsg::getFromId, userId)
                .or().eq(ChatMsg::getToId, userId))
                .orderByDesc(ChatMsg::getCreateTime);

        // 查询所有相关聊天记录
        List<ChatMsg> chatMessages = chatMsgService.list(queryWrapper);

        // 将聊天记录按会话分组，取每个会话的最新一条消息
        // 会话标识：单聊是对方用户ID，群聊是群组ID（toId）
        Map<String, ChatMsg> sessionMap = new LinkedHashMap<>();

        for (ChatMsg message : chatMessages) {
            String sessionId = "";
            if ("group".equals(message.getMsgType())) {
                // 群聊：会话ID是toId
                sessionId = message.getToId();
            } else {
                // 单聊：会话ID是对方用户ID
                sessionId = message.getFromId().equals(userId) ? message.getToId() : message.getFromId();
            }

            // 如果会话不存在，则将当前消息加入
            if (!sessionMap.containsKey(sessionId)) {
                sessionMap.put(sessionId, message);
            }
            // 否则保留第一条（因为已经按时间倒序排列）
        }

        // 转换为会话列表
        List<ChatMsg> sessionList = new ArrayList<>(sessionMap.values());

        return ResponseResult.success(sessionList);
    }

    /**
     * 获取聊天记录
     * @param sessionId 会话ID，可以是：
     *                  1. 单聊：两个用户ID用"_"拼接，如"user1_user2"
     *                  2. 群聊：群组ID，如"group123"
     */
    @ApiOperation("获取聊天记录")
    @GetMapping("/chat/{sessionId}/messages")
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

        // 按时间倒序排序
        queryWrapper.orderByDesc(ChatMsg::getCreateTime);

        // 查询结果
        Page<ChatMsg> result = chatMsgService.page(chatMsgPage, queryWrapper);
        return ResponseResult.success(result);
    }

    /**
     * 发送消息
     */
    @ApiOperation("发送消息")
    @PostMapping("/chat/messages")
    public ResponseResult<?> sendMessage(@RequestBody ChatMsg chatMsg) {
        // 设置默认值
        chatMsg.setReadStatus(false);
        chatMsg.setCreateTime(LocalDateTime.now());

        // 保存消息
        boolean success = chatMsgService.save(chatMsg);
        if (success) {
            return ResponseResult.success("消息发送成功");
        } else {
            return ResponseResult.fail("500", "消息发送失败");
        }
    }

    /**
     * 标记消息已读
     */
    @ApiOperation("标记消息已读")
    @PutMapping("/chat/messages/{messageId}/read")
    public ResponseResult<?> markMessageAsRead(@PathVariable Long messageId) {
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
}

