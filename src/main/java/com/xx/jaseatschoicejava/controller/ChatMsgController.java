package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 */
@RestController
@RequestMapping("/chat-messages")
public class ChatMsgController {

    @Autowired
    private ChatMsgService chatMsgService;

    /**
     * 发送消息
     */
    @PostMapping
    public ResponseResult<?> sendMessage(@RequestBody ChatMsg message) {
        boolean success = chatMsgService.save(message);
        if (success) {
            return ResponseResult.success(message.getId());
        }
        return ResponseResult.fail("500", "发送消息失败");
    }

    /**
     * 获取对话消息
     */
    @GetMapping("/conversation")
    public ResponseResult<?> getConversationMessages(@RequestParam String fromId, @RequestParam String toId) {
        LambdaQueryWrapper<ChatMsg> queryWrapper = new LambdaQueryWrapper<>();
        // 查询从fromId到toId和从toId到fromId的所有消息
        queryWrapper.and(wrapper -> wrapper
                .eq(ChatMsg::getFromId, fromId)
                .eq(ChatMsg::getToId, toId)
        ).or(wrapper -> wrapper
                .eq(ChatMsg::getFromId, toId)
                .eq(ChatMsg::getToId, fromId)
        );
        queryWrapper.orderByAsc(ChatMsg::getCreateTime); // 按创建时间升序排列
        List<ChatMsg> messages = chatMsgService.list(queryWrapper);

        // 标记未读消息为已读
        messages.stream()
                .filter(msg -> msg.getToId().equals(fromId) && !msg.getReadStatus())
                .forEach(msg -> {
                    msg.setReadStatus(true);
                    chatMsgService.updateById(msg);
                });

        return ResponseResult.success(messages);
    }
}
