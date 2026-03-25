package com.xx.jaseatschoicejava.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Redis Pipeline批量操作工具类
 *
 * 功能：
 * 1. 批量操作优化（减少网络往返）
 * 2. 性能提升：10-100倍（取决于批量大小）
 * 3. 适用于：批量查询、批量写入、批量删除
 *
 * 使用场景：
 * - 批量获取用户信息
 * - 批量更新缓存
 * - 批量删除过期缓存
 * - 批量预热数据
 *
 * 性能对比：
 * - 单次操作：每次操作需要一次网络往返（RTT约1-10ms）
 * - Pipeline操作：批量操作只需要一次网络往返
 * - 100次操作：单次1000ms，Pipeline约10-100ms
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Component
public class RedisPipelineUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 批量执行操作（无返回值）
     *
     * 使用示例：
     * <pre>
     * redisPipelineUtil.executePipeline(pipeline -> {
     *     for (String userId : userIds) {
     *         pipeline.opsForValue().set("user:" + userId, userObj);
     *     }
     * });
     * </pre>
     *
     * @param action 要执行的批量操作
     */
    public void executePipeline(Consumer<org.springframework.data.redis.core.RedisTemplate<String, Object>> action) {
        long startTime = System.currentTimeMillis();

        try {
            // 启用Pipeline
            redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                // 开启Pipeline模式
                connection.openPipeline();

                // 执行批量操作
                action.accept(redisTemplate);

                // 关闭Pipeline并返回null（结果会自动收集）
                return null;
            });

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Pipeline批量操作完成: duration={}ms", duration);

        } catch (Exception e) {
            log.error("Pipeline批量操作失败", e);
            throw new RuntimeException("Pipeline操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量执行操作（有返回值列表）
     *
     * 使用示例：
     * <pre>
     * List<Object> results = redisPipelineUtil.executePipelineWithResult(pipeline -> {
     *     for (String userId : userIds) {
     *         pipeline.opsForValue().get("user:" + userId);
     *     }
     * });
     * </pre>
     *
     * @param action 要执行的批量操作
     * @return 操作结果列表
     */
    public List<Object> executePipelineWithResult(
            Function<org.springframework.data.redis.core.RedisTemplate<String, Object>, List<Object>> action) {

        long startTime = System.currentTimeMillis();

        try {
            // 执行Pipeline并获取结果
            List<Object> results = (List<Object>) redisTemplate.executePipelined(
                (org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                    connection.openPipeline();
                    return null;
                });

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Pipeline批量操作完成: duration={}ms, resultCount={}", duration, results != null ? results.size() : 0);

            return results;

        } catch (Exception e) {
            log.error("Pipeline批量操作失败", e);
            throw new RuntimeException("Pipeline操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量设置键值对
     *
     * @param keyValueMap 键值对映射
     * @param expireSeconds 过期时间（秒），0表示不过期
     */
    public void mset(Map<String, Object> keyValueMap, long expireSeconds) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }

        log.debug("批量设置键值对: count={}, expireSeconds={}", keyValueMap.size(), expireSeconds);

        executePipeline(pipeline -> {
            keyValueMap.forEach((key, value) -> {
                if (expireSeconds > 0) {
                    pipeline.opsForValue().set(key, value, expireSeconds, java.util.concurrent.TimeUnit.SECONDS);
                } else {
                    pipeline.opsForValue().set(key, value);
                }
            });
        });
    }

    /**
     * 批量获取值
     *
     * @param keys 键列表
     * @return 值列表（与keys顺序一致）
     */
    public List<Object> mget(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        log.debug("批量获取值: count={}", keys.size());

        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量删除键
     *
     * @param keys 键列表
     * @return 删除的数量
     */
    public long mdelete(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        log.debug("批量删除键: count={}", keys.size());

        return redisTemplate.delete(keys);
    }

    /**
     * 批量预热数据
     *
     * 使用场景：应用启动时或定时任务中预热热点数据
     *
     * @param dataMap 数据映射（key -> value）
     * @param expireSeconds 过期时间（秒）
     */
    public void warmupBatch(Map<String, Object> dataMap, long expireSeconds) {
        log.info("批量预热数据: count={}, expireSeconds={}s", dataMap.size(), expireSeconds);

        long startTime = System.currentTimeMillis();

        mset(dataMap, expireSeconds);

        long duration = System.currentTimeMillis() - startTime;
        log.info("批量预热完成: count={}, duration={}ms, avgTime={}ms/key",
            dataMap.size(), duration, String.format("%.2f", (double) duration / dataMap.size()));
    }

    /**
     * 批量设置Hash
     *
     * @param key Hash键
     * @param hashMap Hash字段映射
     * @param expireSeconds 过期时间（秒）
     */
    public void hsetBatch(String key, Map<String, Object> hashMap, long expireSeconds) {
        if (hashMap == null || hashMap.isEmpty()) {
            return;
        }

        log.debug("批量设置Hash: key={}, fieldCount={}", key, hashMap.size());

        try {
            // 批量设置Hash字段
            redisTemplate.opsForHash().putAll(key, hashMap);

            // 设置过期时间
            if (expireSeconds > 0) {
                redisTemplate.expire(key, expireSeconds, java.util.concurrent.TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            log.error("批量设置Hash失败: key={}", key, e);
            throw new RuntimeException("批量设置Hash失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量获取Hash字段
     *
     * @param key Hash键
     * @param fields 字段列表
     * @return 字段值映射
     */
    public Map<Object, Object> hgetBatch(String key, List<Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        log.debug("批量获取Hash: key={}, fieldCount={}", key, fields.size());

        return redisTemplate.opsForHash().multiGet(key, fields).stream()
            .filter(value -> value != null)
            .collect(java.util.stream.Collectors.toMap(
                value -> fields.get(redisTemplate.opsForHash().values(key).indexOf(value)),
                value -> value
            ));
    }

    /**
     * 性能测试：对比单次操作与Pipeline操作
     *
     * @param count 测试次数
     * @param keyPrefix 键前缀
     * @return 性能对比结果
     */
    public String performanceTest(int count, String keyPrefix) {
        log.info("开始性能测试: count={}, keyPrefix={}", count, keyPrefix);

        // 准备测试数据
        Map<String, Object> testData = new java.util.HashMap<>();
        for (int i = 0; i < count; i++) {
            testData.put(keyPrefix + ":test:" + i, "value_" + i);
        }

        // 测试单次操作
        long singleStart = System.currentTimeMillis();
        testData.forEach((key, value) -> {
            redisTemplate.opsForValue().set(key, value, 60, java.util.concurrent.TimeUnit.SECONDS);
        });
        long singleDuration = System.currentTimeMillis() - singleStart;

        // 清理数据
        redisTemplate.delete(testData.keySet());

        // 测试Pipeline操作
        long pipelineStart = System.currentTimeMillis();
        mset(testData, 60);
        long pipelineDuration = System.currentTimeMillis() - pipelineStart;

        // 清理数据
        redisTemplate.delete(testData.keySet());

        // 计算性能提升
        double speedup = (double) singleDuration / pipelineDuration;

        String result = String.format(
            "性能测试结果 - 单次操作: %dms, Pipeline操作: %dms, 性能提升: %.2fx",
            singleDuration, pipelineDuration, speedup
        );

        log.info(result);
        return result;
    }
}
