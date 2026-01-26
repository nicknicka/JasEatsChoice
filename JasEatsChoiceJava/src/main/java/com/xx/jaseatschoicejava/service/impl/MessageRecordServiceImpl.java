package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.MessageRecord;
import com.xx.jaseatschoicejava.mapper.MessageRecordMapper;
import com.xx.jaseatschoicejava.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息记录Service实现
 *
 * @author xx
 * @date 2025-01-26
 */
@Service
public class MessageRecordServiceImpl extends ServiceImpl<MessageRecordMapper, MessageRecord> implements MessageRecordService {

    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Override
    public List<MessageRecord> getMessageRecordsByUserId(String userId, Integer pageSize, Integer pageNum) {
        // 计算分页起始索引
        int startIndex = (pageNum - 1) * pageSize;
        // 直接使用 MyBatis-Plus 的 lambdaQuery 查询，避免 XML 映射问题
        return lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(MessageRecord::getReceiverId, userId)
                        .or()
                        .eq(MessageRecord::getSenderId, userId))
                .orderByDesc(MessageRecord::getSendTime)
                .last("LIMIT " + pageSize + " OFFSET " + startIndex)
                .list();
    }

    @Override
    public Integer getUnreadMessageCountByUserId(String userId) {
        // 使用 MyBatis-Plus 的 lambdaQuery 查询未读消息数量
        return Math.toIntExact(lambdaQuery()
                .eq(MessageRecord::getReceiverId, userId)
                .eq(MessageRecord::getReadStatus, 0)
                .count());
    }

    @Override
    public Boolean sendMessage(String senderId, String receiverId, String content, String messageType) {
        // 创建消息记录对象
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setSenderId(senderId);
        messageRecord.setReceiverId(receiverId);
        messageRecord.setContent(content);
        messageRecord.setMessageType(messageType);
        messageRecord.setSendTime(LocalDateTime.now());
        messageRecord.setReadStatus(0); // 默认未读

        // 保存到数据库
        return this.save(messageRecord);
    }

    @Override
    public Boolean markMessageAsRead(String messageId) {
        // 创建更新对象
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setId(messageId);
        messageRecord.setReadStatus(1); // 已读状态

        // 更新到数据库
        return this.updateById(messageRecord);
    }

    @Override
    public Boolean markAllMessagesAsRead(String userId) {
        // 更新所有接收者为当前用户的消息为已读
        return lambdaUpdate()
                .eq(MessageRecord::getReceiverId, userId)
                .set(MessageRecord::getReadStatus, 1)
                .update();
    }

    @Override
    public Boolean deleteMessage(String messageId) {
        // 删除单条消息
        return this.removeById(messageId);
    }

    @Override
    public Boolean batchDeleteMessages(List<String> messageIds) {
        // 批量删除消息
        if (messageIds == null || messageIds.isEmpty()) {
            return false;
        }
        return this.removeByIds(messageIds);
    }
}
