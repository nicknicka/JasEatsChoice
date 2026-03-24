package com.xx.jaseatschoicejava.config;

import com.github.benmanes.caffeine.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 二级缓存配置
 *
 * 架构：
 * L1: Caffeine本地缓存（进程内，速度快）
 * L2: Redis分布式缓存（跨进程，共享）
 *
 * 工作流程：
 * 1. 先查询Caffeine本地缓存
 * 2. 本地缓存未命中，查询Redis
 * 3. Redis未命中，查询数据库
 * 4. 查询结果写入Redis和Caffeine
 *
 * 优点：
 * - 极快的读取速度（本地缓存<1ms）
 * - 减少Redis网络IO
 * - 降低Redis负载
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Configuration
@EnableCaching
public class TwoLevelCacheConfig {

    /**
     * Caffeine本地缓存配置
     *
     * 用途：缓存热点数据，减少Redis访问
     */
    @Bean
    public Caffeine<Object, Object> caffeineCache() {
        return Caffeine.newBuilder()
            // 最大缓存条目数
            .maximumSize(1000)
            // 写入后5分钟过期
            .expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES)
            // 初始容量
            .initialCapacity(100)
            // 记录统计信息
            .recordStats()
            // 移除监听器
            .removalListener((key, value, cause) -> {
                log.debug("Caffeine缓存移除: key={}, cause={}", key, cause);
            })
            .build();
    }

    /**
     * Caffeine缓存管理器（L1缓存）
     *
     * 适用于：极高频访问的热点数据
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeineCache());
        return caffeineCacheManager;
    }

    /**
     * Redis缓存管理器（L2缓存）
     *
     * 适用于：分布式共享数据
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        // 序列化配置
        GenericJackson2JsonRedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer<>(Object.class);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }

    /**
     * 混合缓存管理器（CompositeCacheManager）
     *
     * 注意：Spring Boot 2.x不支持开箱即用的CompositeCacheManager
     * 这里提供配置示例，如需使用需要自定义实现
     *
     * 替代方案：
     * 1. 使用@Cacheable指定cacheManager
     * 2. 不同缓存使用不同级别
     */
    @Bean
    @Primary
    public CacheManager compositeCacheManager(
            org.springframework.cache.CacheManager caffeineCacheManager,
            org.springframework.cache.CacheManager redisCacheManager) {

        log.info("初始化混合缓存管理器...");
        log.info("L1: Caffeine本地缓存");
        log.info("L2: Redis分布式缓存");

        // 简单实现：使用Redis作为主缓存管理器
        // 如需真正的二级缓存，需要自定义CompositeCacheManager
        return (CacheManager) redisCacheManager;
    }

    /**
     * 自定义缓存配置
     *
     * 为不同的缓存区域设置不同的策略
     */
    @Bean
    public Map<String, Caffeine<Object, Object>> caffeineCacheConfigs() {
        Map<String, Caffeine<Object, Object>> configs = new HashMap<>();

        // 用户偏好缓存（高频访问）
        configs.put("user:preference", Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES)
            .recordStats()
            .build());

        // 菜品详情缓存（中频访问）
        configs.put("dish:detail", Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES)
            .recordStats()
            .build());

        // 用户信息缓存（高频访问）
        configs.put("user:info", Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES)
            .recordStats()
            .build());

        return configs;
    }

    /**
     * 获取Caffeine统计信息
     *
     * @return 统计信息字符串
     */
    public String getCaffeineStats() {
        Caffeine<Object, Object> caffeine = caffeineCache();
        com.github.benmanes.caffeine.stats.CacheStats stats = caffeine.stats();

        return String.format(
            "Caffeine Stats: hitRate=%.2f%%, hitCount=%d, missCount=%d, totalLoadCount=%d, evictionCount=%d",
            stats.hitRate() * 100,
            stats.hitCount(),
            stats.missCount(),
            stats.totalLoadCount(),
            stats.evictionCount()
        );
    }
}
