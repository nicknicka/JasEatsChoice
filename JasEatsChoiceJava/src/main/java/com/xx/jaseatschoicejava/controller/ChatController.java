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

        // 保存消息
        boolean success = chatMsgService.save(chatMsg);
        if (success) {
            return ResponseResult.success(chatMsg);
        } else {
            return ResponseResult.fail("500", "消息发送失败");
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

