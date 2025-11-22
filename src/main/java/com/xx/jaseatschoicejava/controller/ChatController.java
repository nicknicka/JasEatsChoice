package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        // TODO: 需要根据业务逻辑实现会话列表查询
        // 会话列表应该是与该用户有过聊天记录的所有用户/群组的列表
        // 这里先返回空列表，后续需要完善
        return ResponseResult.success("会话列表获取成功");
    }

    /**
     * 获取聊天记录
     */
    @ApiOperation("获取聊天记录")
    @GetMapping("/chat/{sessionId}/messages")
    public ResponseResult<?> getChatMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // TODO: 这里sessionId需要明确是单聊还是群聊
        // 单聊的话，sessionId可以是两个用户ID的组合
        // 群聊的话，sessionId是群组ID

        // 这里暂时假设是根据fromId和toId的组合查询，需要根据实际业务调整
        Page<ChatMsg> chatMsgPage = new Page<>(page, size);

        // 这里查询逻辑需要根据实际业务调整，例如查询两个人之间的聊天记录
        // LambdaQueryWrapper<ChatMsg> queryWrapper = new LambdaQueryWrapper<>();
        // queryWrapper.and(wrapper -> wrapper
        //         .eq(ChatMsg::getFromId, sessionId.split("-")[0])
        //         .eq(ChatMsg::getToId, sessionId.split("-")[1]))
        //         .or(wrapper -> wrapper
        //                 .eq(ChatMsg::getFromId, sessionId.split("-")[1])
        //                 .eq(ChatMsg::getToId, sessionId.split("-")[0]))
        //         .orderByDesc(ChatMsg::getCreateTime);

        // 暂时返回空结果，等待业务逻辑明确
        Page<ChatMsg> result = chatMsgService.page(chatMsgPage, null);
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

