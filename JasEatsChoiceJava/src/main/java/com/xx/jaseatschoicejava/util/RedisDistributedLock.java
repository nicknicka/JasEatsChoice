package com.xx.jaseatschoicejava.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 *
 * 功能：
 * 1. 防止缓存击穿（热点数据并发查询）
 * 2. 防止重复提交（表单重复提交）
 * 3. 互斥访问（临界资源保护）
 *
 * 实现原理：
 * 使用Redis的SETNX（SET if Not eXists）命令实现
 * key不存在时设置成功并返回true，否则返回false
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Component
public class RedisDistributedLock {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 锁前缀
     */
    private static final String LOCK_PREFIX = "lock:";

    /**
     * 默认锁过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 30;

    /**
     * 默认等待时间（毫秒）
     */
    private static final long DEFAULT_WAIT_TIME = 3000;

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 尝试获取锁（指定过期时间）
     *
     * @param lockKey 锁的key
     * @param expireTime 过期时间（秒）
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock(String lockKey, long expireTime) {
        String fullKey = LOCK_PREFIX + lockKey;

        try {
            // SETNX命令：SET if Not eXists
            Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(fullKey, "1", expireTime, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(success)) {
                log.debug("获取分布式锁成功: key={}", fullKey);
                return true;
            } else {
                log.debug("获取分布式锁失败: key={}", fullKey);
                return false;
            }

        } catch (Exception e) {
            log.error("获取分布式锁异常: key={}", fullKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;

        try {
            stringRedisTemplate.delete(fullKey);
            log.debug("释放分布式锁: key={}", fullKey);

        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}", fullKey, e);
        }
    }

    /**
     * 强制释放锁（不管是否持有）
     *
     * @param lockKey 锁的key
     */
    public void forceUnlock(String lockKey) {
        unlock(lockKey);
    }

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁的key
     * @return true-锁存在，false-锁不存在
     */
    public boolean isLocked(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;

        try {
            Boolean hasKey = stringRedisTemplate.hasKey(fullKey);
            return Boolean.TRUE.equals(hasKey);

        } catch (Exception e) {
            log.error("检查锁状态异常: key={}", fullKey, e);
            return false;
        }
    }

    /**
     * 延长锁的过期时间
     *
     * @param lockKey 锁的key
     * @param expireTime 新的过期时间（秒）
     * @return true-延长成功，false-延长失败
     */
    public boolean renewLock(String lockKey, long expireTime) {
        String fullKey = LOCK_PREFIX + lockKey;

        try {
            Boolean success = stringRedisTemplate.expire(fullKey, expireTime, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(success)) {
                log.debug("延长锁过期时间: key={}, expireTime={}s", fullKey, expireTime);
                return true;
            } else {
                log.warn("延长锁过期时间失败: key={}, 锁可能已过期", fullKey);
                return false;
            }

        } catch (Exception e) {
            log.error("延长锁过期时间异常: key={}", fullKey, e);
            return false;
        }
    }

    /**
     * 在锁保护下执行任务
     *
     * 使用模式：
     * <pre>
     * String result = redisDistributedLock.executeWithLock("myLock", 30, () -> {
     *     // 执行需要保护的代码
     *     return "success";
     * });
     * </pre>
     *
     * @param lockKey 锁的key
     * @param expireTime 锁过期时间（秒）
     * @param task 要执行的任务
     * @param <T> 返回值类型
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String lockKey, long expireTime, java.util.function.Supplier<T> task) {
        String fullKey = LOCK_PREFIX + lockKey;

        try {
            // 尝试获取锁
            if (!tryLock(lockKey, expireTime)) {
                log.warn("无法获取锁，任务取消: key={}", fullKey);
                throw new RuntimeException("无法获取锁: " + fullKey);
            }

            // 执行任务
            log.debug("在锁保护下执行任务: key={}", fullKey);
            return task.get();

        } finally {
            // 释放锁
            unlock(lockKey);
        }
    }

    /**
     * 在锁保护下执行任务（使用默认过期时间）
     *
     * @param lockKey 锁的key
     * @param task 要执行的任务
     * @param <T> 返回值类型
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String lockKey, java.util.function.Supplier<T> task) {
        return executeWithLock(lockKey, DEFAULT_EXPIRE_TIME, task);
    }

    /**
     * 尝试获取锁并等待（可配置等待时间）
     *
     * @param lockKey 锁的key
     * @param expireTime 锁过期时间（秒）
     * @param maxWaitTime 最大等待时间（毫秒）
     * @param waitInterval 重试间隔（毫秒）
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLockWithWait(String lockKey, long expireTime, long maxWaitTime, long waitInterval) {
        String fullKey = LOCK_PREFIX + lockKey;
        long startTime = System.currentTimeMillis();

        try {
            while (true) {
                // 尝试获取锁
                if (tryLock(lockKey, expireTime)) {
                    log.debug("获取分布式锁成功（等待后）: key={}", fullKey);
                    return true;
                }

                // 检查是否超时
                if (System.currentTimeMillis() - startTime > maxWaitTime) {
                    log.debug("获取分布式锁超时: key={}, waitTime={}ms", fullKey, maxWaitTime);
                    return false;
                }

                // 等待后重试
                Thread.sleep(waitInterval);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: key={}", fullKey, e);
            return false;
        } catch (Exception e) {
            log.error("获取分布式锁异常: key={}", fullKey, e);
            return false;
        }
    }

    /**
     * 获取锁的剩余过期时间
     *
     * @param lockKey 锁的key
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示不存在
     */
    public long getLockExpireTime(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;

        try {
            Long expire = stringRedisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
            return expire != null ? expire : -2;

        } catch (Exception e) {
            log.error("获取锁过期时间异常: key={}", fullKey, e);
            return -2;
        }
    }

    /**
     * 缓存击穿防护查询
     *
     * 使用场景：热点数据查询，防止缓存失效时大量请求直接打到数据库
     *
     * @param lockKey 锁的key
     * @param query 查询逻辑
     * @param expireTime 锁过期时间（秒）
     * @param <T> 返回值类型
     * @return 查询结果
     */
    public <T> T queryWithLock(String lockKey, java.util.function.Supplier<T> query, long expireTime) {
        return executeWithLock(lockKey, expireTime, query);
    }

    /**
     * 防止重复提交
     *
     * 使用场景：表单提交、订单创建等，防止用户重复点击
     *
     * @param lockKey 锁的key（建议使用唯一标识，如：submit:order:{userId}:{timestamp}）
     * @param task 提交任务
     * @param <T> 返回值类型
     * @return 任务执行结果，如果获取锁失败返回null
     */
    public <T> T preventDuplicateSubmit(String lockKey, java.util.function.Supplier<T> task) {
        // 防重复提交的锁过期时间较短（5秒）
        long shortExpireTime = 5;

        if (!tryLock(lockKey, shortExpireTime)) {
            log.warn("检测到重复提交: key={}", lockKey);
            return null;
        }

        try {
            return task.get();

        } finally {
            unlock(lockKey);
        }
    }
}
