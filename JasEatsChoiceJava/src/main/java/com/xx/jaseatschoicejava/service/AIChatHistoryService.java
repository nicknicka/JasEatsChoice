package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.AIChatHistory;
import com.xx.jaseatschoicejava.mapper.AIChatHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI聊天历史服务类
 */
@Slf4j
@Service
public class AIChatHistoryService {

    @Resource
    private AIChatHistoryMapper aiChatHistoryMapper;

    /**
     * 保存聊天消息
     * @param userId 用户ID
     * @param sender 发送者（user/ai）
     * @param content 消息内容
     */
    public void saveMessage(String userId, String sender, String content) {
        saveMessage(userId, sender, content, null, null);
    }

    /**
     * 保存聊天消息（支持卡片数据）
     * @param userId 用户ID
     * @param sender 发送者（user/ai）
     * @param content 消息内容
     * @param messageType 消息类型（用于卡片显示）
     * @param cardData 卡片数据（JSON格式）
     */
    public void saveMessage(String userId, String sender, String content, String messageType, String cardData) {
        try {
            AIChatHistory chatHistory = new AIChatHistory();
            chatHistory.setUserId(userId);
            chatHistory.setSender(sender);
            chatHistory.setContent(content);
            chatHistory.setCreateTime(LocalDateTime.now());
            chatHistory.setMessageType(messageType);
            chatHistory.setCardData(cardData);

            aiChatHistoryMapper.insert(chatHistory);
            log.debug("保存AI聊天消息成功: userId={}, sender={}, messageType={}", userId, sender, messageType);
        } catch (Exception e) {
            log.error("保存AI聊天消息失败: userId={}, sender={}", userId, sender, e);
        }
    }

    /**
     * 获取用户的所有聊天历史
     * @param userId 用户ID
     * @return 聊天历史列表
     */
    public List<AIChatHistory> getUserChatHistory(String userId) {
        try {
            QueryWrapper<AIChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByAsc("create_time");

            List<AIChatHistory> historyList = aiChatHistoryMapper.selectList(queryWrapper);
            log.info("获取用户AI聊天历史成功: userId={}, 消息数量={}", userId, historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("获取用户AI聊天历史失败: userId={}", userId, e);
            throw new RuntimeException("获取聊天历史失败", e);
        }
    }

    /**
     * 删除用户的所有聊天记录
     * @param userId 用户ID
     */
    public void deleteUserChatHistory(String userId) {
        try {
            // 使用MyBatis-Plus的QueryWrapper删除，无需自定义SQL
            QueryWrapper<AIChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            int deletedCount = aiChatHistoryMapper.delete(queryWrapper);
            log.info("删除用户AI聊天历史成功: userId={}, 删除数量={}", userId, deletedCount);
        } catch (Exception e) {
            log.error("删除用户AI聊天历史失败: userId={}", userId, e);
            throw new RuntimeException("删除聊天历史失败", e);
        }
    }

    /**
     * 判断用户是否有聊天历史
     * @param userId 用户ID
     * @return true-有历史记录，false-无历史记录
     */
    public boolean hasChatHistory(String userId) {
        try {
            QueryWrapper<AIChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            Long count = aiChatHistoryMapper.selectCount(queryWrapper);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查用户聊天历史失败: userId={}", userId, e);
            return false;
        }
    }
}
