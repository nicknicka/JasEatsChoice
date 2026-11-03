package com.xx.jaseatschoicejava.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.xx.jaseatschoicejava.config.AiQuotaProperties;
import com.xx.jaseatschoicejava.exception.AiQuotaExceededException;

/**
 * 基于 Redis 固定窗口的 AI 用户级配额。
 */
@Service
public class AiQuotaService {

    private static final String KEY_PREFIX = "jaseats:ai:quota:";

    private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>(
        """
        local current = redis.call('INCR', KEYS[1])
        if current == 1 then
            redis.call('PEXPIRE', KEYS[1], ARGV[1])
        end
        return current
        """,
        Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final AiQuotaProperties properties;

    public AiQuotaService(StringRedisTemplate redisTemplate, AiQuotaProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * 消耗一次配额；超过窗口限额时抛出 {@link AiQuotaExceededException}。
     *
     * @param userId JWT 过滤器校验后写入的可靠用户 ID
     * @param scope  AI 能力类型
     */
    public void consume(String userId, AiQuotaScope scope) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        Objects.requireNonNull(scope, "scope 不能为空");

        AiQuotaProperties.QuotaLimit quota = limitFor(scope);
        String key = KEY_PREFIX + scope.key() + ":" + userId;
        Long current = redisTemplate.execute(
            CONSUME_SCRIPT,
            List.of(key),
            String.valueOf(quota.getWindow().toMillis())
        );

        if (current == null) {
            throw new IllegalStateException("AI 配额计数失败");
        }
        if (current > quota.getLimit()) {
            throw new AiQuotaExceededException();
        }
    }

    private AiQuotaProperties.QuotaLimit limitFor(AiQuotaScope scope) {
        return switch (scope) {
            case DISH_RECOGNITION -> properties.getDishRecognition();
            case RECIPE -> properties.getRecipe();
        };
    }
}
