package com.xx.jaseatschoicejava.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 *
 * 功能：
 * 1. 封装常用缓存操作（get、set、delete等）
 * 2. 实现缓存穿透防护（缓存空值）
 * 3. 实现缓存雪崩防护（随机过期时间）
 * 4. 提供便捷的缓存操作方法
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Component
public class RedisCacheUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 默认过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 1800; // 30分钟

    /**
     * 缓存空值的过期时间（秒）
     */
    private static final long NULL_VALUE_EXPIRE_SECONDS = 300; // 5分钟

    /**
     * 空值标识
     */
    private static final Object NULL_VALUE = new Object();

    /**
     * 缓存前缀
     */
    private static final String CACHE_PREFIX = "jaseats:cache:";

    /**
     * 获取完整的缓存key
     *
     * @param key 原始key
     * @return 完整的缓存key
     */
    private String getFullKey(String key) {
        return CACHE_PREFIX + key;
    }

    /**
     * 设置缓存（使用默认过期时间）
     *
     * @param key   缓存key
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 设置缓存（指定过期时间）
     *
     * @param key     缓存key
     * @param value   缓存值
     * @param seconds 过期时间（秒）
     */
    public void set(String key, Object value, long seconds) {
        try {
            String fullKey = getFullKey(key);
            redisTemplate.opsForValue().set(fullKey, value, seconds, TimeUnit.SECONDS);
            log.debug("设置缓存成功: key={}, ttl={}秒", fullKey, seconds);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }

    /**
     * 设置缓存（防止缓存穿透）
     *
     * 如果value为null，会缓存一个空值标识，短期过期
     *
     * @param key     缓存key
     * @param value   缓存值
     * @param seconds 过期时间（秒）
     */
    public void setWithNullProtection(String key, Object value, long seconds) {
        try {
            String fullKey = getFullKey(key);
            if (value == null) {
                // 缓存空值，短期过期
                redisTemplate.opsForValue().set(fullKey, NULL_VALUE, NULL_VALUE_EXPIRE_SECONDS, TimeUnit.SECONDS);
                log.debug("设置空值缓存: key={}, ttl={}秒", fullKey, NULL_VALUE_EXPIRE_SECONDS);
            } else {
                // 添加随机过期时间，防止缓存雪崩
                long randomTtl = seconds + (long) (Math.random() * 60); // 添加0-60秒随机时间
                redisTemplate.opsForValue().set(fullKey, value, randomTtl, TimeUnit.SECONDS);
                log.debug("设置缓存成功: key={}, ttl={}秒（含随机时间）", fullKey, randomTtl);
            }
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存key
     * @return 缓存值，如果不存在或为空值标识则返回null
     */
    public Object get(String key) {
        try {
            String fullKey = getFullKey(key);
            Object value = redisTemplate.opsForValue().get(fullKey);

            // 如果是空值标识，返回null
            if (value == NULL_VALUE) {
                log.debug("获取到空值缓存: key={}", fullKey);
                return null;
            }

            log.debug("获取缓存: key={}, hit={}", fullKey, value != null);
            return value;
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存（指定类型）
     *
     * @param key 缓存key
     * @param clazz 返回值类型
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value != null && clazz != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     */
    public void delete(String key) {
        try {
            String fullKey = getFullKey(key);
            redisTemplate.delete(fullKey);
            log.debug("删除缓存: key={}", fullKey);
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存key集合
     */
    public void delete(Collection<String> keys) {
        try {
            String[] fullKeys = keys.stream()
                .map(this::getFullKey)
                .toArray(String[]::new);
            redisTemplate.delete(java.util.Arrays.asList(fullKeys));
            log.debug("批量删除缓存: count={}", keys.size());
        } catch (Exception e) {
            log.error("批量删除缓存失败", e);
        }
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存key
     * @return true-存在，false-不存在
     */
    public boolean exists(String key) {
        try {
            String fullKey = getFullKey(key);
            Boolean exist = redisTemplate.hasKey(fullKey);
            return Boolean.TRUE.equals(exist);
        } catch (Exception e) {
            log.error("判断缓存是否存在失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key     缓存key
     * @param seconds 过期时间（秒）
     * @return true-设置成功，false-设置失败
     */
    public boolean expire(String key, long seconds) {
        try {
            String fullKey = getFullKey(key);
            Boolean result = redisTemplate.expire(fullKey, seconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取剩余过期时间
     *
     * @param key 缓存key
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示不存在
     */
    public long getExpire(String key) {
        try {
            String fullKey = getFullKey(key);
            Long expire = redisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取过期时间失败: key={}", key, e);
            return -2;
        }
    }

    /**
     * 检查并设置缓存（如果不存在）
     *
     * @param key   缓存key
     * @param value 缓存值
     * @return true-设置成功，false-key已存在
     */
    public boolean setIfAbsent(String key, Object value) {
        try {
            String fullKey = getFullKey(key);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(fullKey, value, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("设置缓存失败（setIfAbsent）: key={}", key, e);
            return false;
        }
    }

    /**
     * 检查并设置缓存（如果不存在，指定过期时间）
     *
     * @param key     缓存key
     * @param value   缓存值
     * @param seconds 过期时间（秒）
     * @return true-设置成功，false-key已存在
     */
    public boolean setIfAbsent(String key, Object value, long seconds) {
        try {
            String fullKey = getFullKey(key);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(fullKey, value, seconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("设置缓存失败（setIfAbsent）: key={}", key, e);
            return false;
        }
    }
}
