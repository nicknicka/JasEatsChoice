package com.xx.jaseatschoicejava.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.Duration;

/**
 * 用户偏好缓存服务
 *
 * 用于缓存UserPreferenceAgent的分析结果，避免重复调用LLM
 *
 * @author Claude
 * @since 2026-03-27
 */
@Slf4j
@Service
public class UserPreferenceCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String CACHE_KEY_PREFIX = "user:preference:";

    // 缓存过期时间（1小时）
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * 获取用户偏好缓存
     *
     * @param userId 用户ID
     * @return 缓存的偏好分析结果，如果不存在或已过期则返回null
     */
    public String getCachedPreference(String userId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                log.info("✅ [缓存命中] 用户偏好缓存命中: userId={}", userId);
                return cached.toString();
            }

            log.debug("⏭️ [缓存未命中] 用户偏好缓存未命中: userId={}", userId);
            return null;
        } catch (Exception e) {
            log.warn("⚠️ [缓存读取失败] userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 保存用户偏好到缓存
     *
     * @param userId 用户ID
     * @param preference 偏好分析结果
     */
    public void cachePreference(String userId, String preference) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(cacheKey, preference, CACHE_TTL);

            log.info("💾 [缓存保存] 用户偏好已缓存: userId={}, TTL={}小时", userId, CACHE_TTL.toHours());
        } catch (Exception e) {
            log.warn("⚠️ [缓存保存失败] userId={}, error={}", userId, e.getMessage());
            // 失败不影响主流程
        }
    }

    /**
     * 清除用户偏好缓存
     *
     * 当用户更新资料时调用此方法，使缓存失效
     *
     * @param userId 用户ID
     */
    public void clearCache(String userId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + userId;
            Boolean deleted = redisTemplate.delete(cacheKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("🗑️ [缓存清除] 用户偏好缓存已清除: userId={}", userId);
            } else {
                log.debug("ℹ️ [缓存清除] 无需清除，缓存不存在: userId={}", userId);
            }
        } catch (Exception e) {
            log.warn("⚠️ [缓存清除失败] userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param userId 用户ID
     * @return true-缓存存在，false-缓存不存在
     */
    public boolean hasCache(String userId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + userId;
            Boolean hasKey = redisTemplate.hasKey(cacheKey);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.warn("⚠️ [缓存检查失败] userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }
}
