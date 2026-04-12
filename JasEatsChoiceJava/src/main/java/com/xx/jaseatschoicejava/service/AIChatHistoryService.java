package com.xx.jaseatschoicejava.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.AIChatHistory;
import com.xx.jaseatschoicejava.mapper.AIChatHistoryMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * AI聊天历史服务类
 *
 * 支持Redis缓存层优化查询性能
 */
@Slf4j
@Service
public class AIChatHistoryService {

    @Resource
    private AIChatHistoryMapper aiChatHistoryMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String REDIS_KEY_PREFIX = "ai:chat:history:";
    private static final String REDIS_DEDUP_KEY_PREFIX = "ai:chat:dedup:";

    // Redis缓存过期时间（2小时）
    private static final long REDIS_CACHE_HOURS = 2;
    private static final long DEDUP_TTL_MINUTES = 10;

    /**
     * 保存聊天消息
     * 使用 @Transactional 确保数据一致性
     *
     * @param userId 用户ID
     * @param sender 发送者（user/ai）
     * @param content 消息内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(String userId, String sender, String content) {
        saveMessage(userId, sender, content, null, null);
    }

    /**
     * 保存聊天消息（支持卡片数据）
     * 同时保存到MySQL和Redis
     *
     * 使用 @Transactional 确保数据一致性：
     * - MySQL 和 Redis 保存要么都成功，要么都失败
     *
     * @param userId 用户ID
     * @param sender 发送者（user/ai）
     * @param content 消息内容
     * @param messageType 消息类型（用于卡片显示）
     * @param cardData 卡片数据（JSON格式）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(String userId, String sender, String content, String messageType, String cardData) {
        saveMessage(userId, sender, content, messageType, cardData, null);
    }

    /**
     * 保存聊天消息（支持幂等）
     *
     * @param userId 用户ID
     * @param sender 发送者（user/ai）
     * @param content 消息内容
     * @param messageType 消息类型
     * @param cardData 卡片数据
     * @param clientMessageId 客户端消息ID（可选，用于幂等）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(String userId, String sender, String content, String messageType, String cardData, String clientMessageId) {
        try {
            if (StringUtils.hasText(clientMessageId)) {
                String dedupKey = buildDedupKey(userId, sender, clientMessageId);
                Boolean firstSeen = redisTemplate.opsForValue().setIfAbsent(
                    Objects.requireNonNull(dedupKey),
                    "1",
                    DEDUP_TTL_MINUTES,
                    TimeUnit.MINUTES);
                if (!Boolean.TRUE.equals(firstSeen)) {
                    log.info("⏭️ [AIChatHistoryService] 命中幂等去重，跳过写库: userId={}, sender={}, clientMessageId={}",
                            userId, sender, clientMessageId);
                    return;
                }
            }

            log.info("==================== 💾 数据库写入开始 ====================");
            log.info("📝 [AIChatHistoryService] 准备保存消息到数据库");
            log.info("📝 [AIChatHistoryService] userId: {}", userId);
            log.info("📝 [AIChatHistoryService] sender: {}", sender);
            log.info("📝 [AIChatHistoryService] clientMessageId: {}", clientMessageId);
            log.info("📝 [AIChatHistoryService] messageType: {}", messageType);
            log.info("📝 [AIChatHistoryService] 内容长度: {} 字符", content != null ? content.length() : 0);
            log.info("📝 [AIChatHistoryService] 完整内容:");
            log.info("─ 开始 ({} 字符) ─", content != null ? content.length() : 0);
            log.info(content != null ? content : "null");
            log.info("─ 结束 ─");
            log.info("📝 [AIChatHistoryService] cardData长度: {} 字符", cardData != null ? cardData.length() : 0);
            if (cardData != null && cardData.length() > 0) {
                log.info("📝 [AIChatHistoryService] cardData内容:");
                log.info("─ 开始 ({} 字符) ─", cardData.length());
                log.info(cardData);
                log.info("─ 结束 ─");
            }
            log.info("=====================================================");

            // 1. 保存到MySQL
            AIChatHistory chatHistory = new AIChatHistory();
            chatHistory.setUserId(userId);
            chatHistory.setSender(sender);
            chatHistory.setContent(content);
            chatHistory.setCreateTime(LocalDateTime.now());
            chatHistory.setMessageType(messageType);
            chatHistory.setCardData(cardData);

            aiChatHistoryMapper.insert(chatHistory);

            log.info("✅ [AIChatHistoryService] MySQL写入成功! ID={}", chatHistory.getId());

            // 2. 保存到Redis缓存
            saveToRedis(userId, chatHistory);

            log.info("=====================================================");
        } catch (Exception e) {
            log.error("❌ [AIChatHistoryService] 保存AI聊天消息失败: userId={}, sender={}", userId, sender, e);
            log.error("❌ [AIChatHistoryService] 错误详情:", e);
        }
    }

    private String buildDedupKey(String userId, String sender, String clientMessageId) {
        return REDIS_DEDUP_KEY_PREFIX + userId + ":" + sender + ":" + clientMessageId;
    }

    /**
     * 获取用户的所有聊天历史
     * 先查Redis缓存，未命中再查MySQL
     *
     * @param userId 用户ID
     * @return 聊天历史列表
     */
    public List<AIChatHistory> getUserChatHistory(String userId) {
        try {
            // 1. 先查Redis缓存
            String redisKey = REDIS_KEY_PREFIX + userId;

            @SuppressWarnings("unchecked")
            List<AIChatHistory> cachedList = (List<AIChatHistory>) redisTemplate.opsForValue().get(redisKey);

            if (cachedList != null && !cachedList.isEmpty()) {
                log.info("✅ [Redis缓存] 命中缓存: userId={}, 消息数量={}", userId, cachedList.size());
                return cachedList;
            }

            log.info("⏭️ [Redis缓存] 缓存未命中，查询MySQL: userId={}", userId);

            // 2. Redis未命中，查询MySQL
            QueryWrapper<AIChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByAsc("create_time");

            List<AIChatHistory> historyList = aiChatHistoryMapper.selectList(queryWrapper);
            if (historyList == null) {
                historyList = new ArrayList<>();
            }

            // 3. 将查询结果写入Redis缓存
            if (!historyList.isEmpty()) {
                saveToRedis(userId, historyList);
                log.info("✅ [Redis缓存] 已写入缓存: userId={}, 消息数量={}", userId, historyList.size());
            }

            log.info("✅ [MySQL] 获取用户AI聊天历史成功: userId={}, 消息数量={}", userId, historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("❌ 获取用户AI聊天历史失败: userId={}", userId, e);
            throw new RuntimeException("获取聊天历史失败", e);
        }
    }

    /**
     * 删除用户的所有聊天记录
     * 同时删除MySQL和Redis缓存
     *
     * @param userId 用户ID
     */
    public void deleteUserChatHistory(String userId) {
        try {
            // 1. 删除MySQL数据
            QueryWrapper<AIChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            int deletedCount = aiChatHistoryMapper.delete(queryWrapper);
            log.info("✅ [MySQL] 删除用户AI聊天历史成功: userId={}, 删除数量={}", userId, deletedCount);

            // 2. 删除Redis缓存
            String redisKey = REDIS_KEY_PREFIX + userId;
            redisTemplate.delete(redisKey);
            log.info("✅ [Redis缓存] 已删除缓存: userId={}", userId);

        } catch (Exception e) {
            log.error("❌ 删除用户AI聊天历史失败: userId={}", userId, e);
            throw new RuntimeException("删除聊天历史失败", e);
        }
    }

    /**
     * 判断用户是否有聊天历史
     * 先查Redis，再查MySQL
     *
     * @param userId 用户ID
     * @return true-有历史记录，false-无历史记录
     */
    public boolean hasChatHistory(String userId) {
        try {
            // 1. 先查Redis
            String redisKey = REDIS_KEY_PREFIX + userId;
            Boolean hasKey = redisTemplate.hasKey(redisKey);

            if (Boolean.TRUE.equals(hasKey)) {
                // Redis中有数据
                Object cached = redisTemplate.opsForValue().get(redisKey);
                if (cached instanceof List) {
                    List<?> list = (List<?>) cached;
                    return !list.isEmpty();
                }
            }

            // 2. 查MySQL
            QueryWrapper<AIChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            Long count = aiChatHistoryMapper.selectCount(queryWrapper);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("❌ 检查用户聊天历史失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 保存单条消息到Redis
     *
     * @param userId 用户ID
     * @param chatHistory 聊天消息
     */
    private void saveToRedis(String userId, AIChatHistory chatHistory) {
        try {
            String redisKey = REDIS_KEY_PREFIX + userId;

            // 获取当前缓存列表
            @SuppressWarnings("unchecked")
            List<AIChatHistory> cachedList = (List<AIChatHistory>) redisTemplate.opsForValue().get(redisKey);

            if (cachedList == null) {
                cachedList = new java.util.ArrayList<>();
            }

            // 添加新消息
            cachedList.add(chatHistory);

            // 保存回Redis
            redisTemplate.opsForValue().set(redisKey, cachedList, REDIS_CACHE_HOURS, TimeUnit.HOURS);

            log.debug("✅ [Redis缓存] 单条消息已保存到缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("⚠️ [Redis缓存] 保存单条消息失败: userId={}, error={}", userId, e.getMessage());
            // 失败不影响主流程
        }
    }

    /**
     * 保存消息列表到Redis
     *
     * @param userId 用户ID
     * @param historyList 聊天历史列表
     */
    private void saveToRedis(String userId, List<AIChatHistory> historyList) {
        try {
            if (historyList == null) {
                return;
            }
            String redisKey = REDIS_KEY_PREFIX + userId;

            // 保存到Redis
            redisTemplate.opsForValue().set(redisKey, historyList, REDIS_CACHE_HOURS, TimeUnit.HOURS);

            log.debug("✅ [Redis缓存] 消息列表已保存到缓存: userId={}, 数量={}", userId, historyList.size());
        } catch (Exception e) {
            log.warn("⚠️ [Redis缓存] 保存消息列表失败: userId={}, error={}", userId, e.getMessage());
            // 失败不影响主流程
        }
    }
}
