package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.MessageRecord;
import com.xx.jaseatschoicejava.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息记录控制器
 *
 * @author xx
 * @date 2025-01-26
 */
@RestController
@RequestMapping("/v1/message")
public class MessageRecordController {

    @Autowired
    private MessageRecordService messageRecordService;

    /**
     * 根据用户ID获取消息记录列表
     *
     * @param userId 用户ID
     * @param pageSize 分页大小（默认10）
     * @param pageNum 当前页码（默认1）
     * @return 消息记录列表
     */
    @GetMapping("/records")
    public ResponseResult<?> getMessageRecords(@RequestParam String userId,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(defaultValue = "1") Integer pageNum) {
        List<MessageRecord> records = messageRecordService.getMessageRecordsByUserId(userId, pageSize, pageNum);
        return ResponseResult.success(records);
    }

    /**
     * 根据用户ID获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    public ResponseResult<?> getUnreadMessageCount(@RequestParam String userId) {
        Integer count = messageRecordService.getUnreadMessageCountByUserId(userId);
        return ResponseResult.success(count);
    }

    /**
     * 根据用户ID获取未读消息数量（兼容前端路径）
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @GetMapping("/records/unread-count")
    public ResponseResult<?> getUnreadMessageCountCompat(@RequestParam String userId) {
        Integer count = messageRecordService.getUnreadMessageCountByUserId(userId);
        return ResponseResult.success(count);
    }

    /**
     * 发送消息
     *
     * @param messageRecord 消息记录对象
     * @return 发送结果
     */
    @PostMapping("/send")
    public ResponseResult<?> sendMessage(@RequestBody MessageRecord messageRecord) {
        Boolean success = messageRecordService.sendMessage(
                messageRecord.getSenderId(),
                messageRecord.getReceiverId(),
                messageRecord.getContent(),
                messageRecord.getMessageType()
        );
        if (success) {
            return ResponseResult.success("消息发送成功");
        } else {
            return ResponseResult.fail("500", "消息发送失败");
        }
    }

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @return 标记结果
     */
    @PutMapping("/records/{messageId}/read")
    public ResponseResult<?> markMessageAsRead(@PathVariable String messageId) {
        Boolean success = messageRecordService.markMessageAsRead(messageId);
        if (success) {
            return ResponseResult.success("消息已标记为已读");
        } else {
            return ResponseResult.fail("500", "消息标记失败");
        }
    }

    /**
     * 标记所有消息为已读
     *
     * @param userId 用户ID
     * @return 标记结果
     */
    @PutMapping("/records/all-read")
    public ResponseResult<?> markAllMessagesAsRead(@RequestParam String userId) {
        Boolean success = messageRecordService.markAllMessagesAsRead(userId);
        if (success) {
            return ResponseResult.success("所有消息已标记为已读");
        } else {
            return ResponseResult.fail("500", "消息标记失败");
        }
    }

    /**
     * 删除单条消息
     *
     * @param messageId 消息ID
     * @return 删除结果
     */
    @DeleteMapping("/records/{messageId}")
    public ResponseResult<?> deleteMessage(@PathVariable String messageId) {
        Boolean success = messageRecordService.deleteMessage(messageId);
        if (success) {
            return ResponseResult.success("消息删除成功");
        } else {
            return ResponseResult.fail("500", "消息删除失败");
        }
    }

    /**
     * 批量删除消息
     *
     * @param messageIds 消息ID列表
     * @return 删除结果
     */
    @DeleteMapping("/records/batch")
    public ResponseResult<?> batchDeleteMessages(@RequestBody List<String> messageIds) {
        Boolean success = messageRecordService.batchDeleteMessages(messageIds);
        if (success) {
            return ResponseResult.success("成功删除 " + messageIds.size() + " 条消息");
        } else {
            return ResponseResult.fail("500", "批量删除失败");
        }
    }
}
