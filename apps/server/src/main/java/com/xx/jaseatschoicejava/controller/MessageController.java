package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.MessageRecord;
import com.xx.jaseatschoicejava.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/v1/legacy/message")
public class MessageController {

    @Autowired
    private MessageRecordService messageRecordService;

    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    public ResponseResult<?> getMessageList(@RequestParam String userId,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(defaultValue = "1") Integer pageNum) {
        try {
            // 从数据库获取消息列表
            List<MessageRecord> messages = messageRecordService.getMessageRecordsByUserId(userId, pageSize, pageNum);
            return ResponseResult.success(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "获取消息列表失败");
        }
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ResponseResult<?> sendMessage(@RequestBody Map<String, Object> message) {
        try {
            // 解析请求参数
            String senderId = message.get("senderId").toString();
            String receiverId = message.get("receiverId").toString();
            String content = message.get("content").toString();
            String messageType = message.containsKey("messageType") ? message.get("messageType").toString() : "text";

            // 发送消息
            Boolean success = messageRecordService.sendMessage(senderId, receiverId, content, messageType);
            if (success) {
                return ResponseResult.success("消息发送成功");
            }
            return ResponseResult.fail("500", "消息发送失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "消息发送失败");
        }
    }
}
