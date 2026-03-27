package com.xx.jaseatschoicejava.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类
 *
 * 功能：
 * 1. 配置RedisTemplate，使用JSON序列化
 * 2. 配置RedisCacheManager，支持Spring Cache注解
 * 3. 设置默认缓存过期时间为30分钟
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置RedisTemplate
     *
     * 使用Jackson2JsonRedisSerializer序列化value
     * 使用StringRedisSerializer序列化key
     *
     * @param factory Redis连接工厂
     * @return RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        log.info("初始化RedisTemplate...");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的
        om.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        // 注册JavaTimeModule以支持LocalDateTime等Java 8日期时间类型
        om.registerModule(new JavaTimeModule());

        serializer.setObjectMapper(om);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(serializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        log.info("RedisTemplate初始化完成");
        return template;
    }

    /**
     * 配置CacheManager
     *
     * 缓存策略：
     * - 默认过期时间：30分钟
     * - 不缓存null值（防止缓存穿透）
     * - 使用JSON序列化
     *
     * @param factory Redis连接工厂
     * @return CacheManager实例
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        log.info("初始化RedisCacheManager...");

        // 配置序列化
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        // 注册JavaTimeModule以支持LocalDateTime等Java 8日期时间类型
        om.registerModule(new JavaTimeModule());
        serializer.setObjectMapper(om);

        // 配置缓存策略
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            // 设置缓存过期时间为30分钟
            .entryTtl(Duration.ofMinutes(30))
            // 使用StringRedisSerializer来序列化key
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            // 使用Jackson2JsonRedisSerializer来序列化value
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(serializer))
            // 不缓存null值（防止缓存穿透，如果需要缓存null值，可以注释掉这行）
            .disableCachingNullValues();

        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            // 支持事务
            .transactionAware()
            .build();

        log.info("RedisCacheManager初始化完成");
        return cacheManager;
    }
}
