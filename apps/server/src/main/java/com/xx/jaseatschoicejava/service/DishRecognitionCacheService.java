package com.xx.jaseatschoicejava.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 菜品识别结果缓存。
 *
 * 缓存键同时包含图片内容哈希和当前视觉模型版本，避免相同图片在不同模型之间复用结果。
 * Redis 不可用时只记录告警并降级为正常模型调用，不影响识别主流程。
 */
@Service
public class DishRecognitionCacheService {

    private static final Logger log = LoggerFactory.getLogger(DishRecognitionCacheService.class);
    private static final String KEY_PREFIX = "jaseats:ai:dish:v1:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final TypeReference<Map<String, Object>> RESULT_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public DishRecognitionCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<Map<String, Object>> find(byte[] imageBytes, String provider, String visionModel,
            long runtimeVersion) {
        String key = cacheKey(imageBytes, provider, visionModel, runtimeVersion);
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, RESULT_TYPE));
        } catch (Exception e) {
            log.warn("读取菜品识别缓存失败，已降级为模型识别：{}", e.getMessage());
            deleteInvalidEntry(key);
            return Optional.empty();
        }
    }

    public void put(byte[] imageBytes, String provider, String visionModel, long runtimeVersion,
            Map<String, Object> result) {
        String key = cacheKey(imageBytes, provider, visionModel, runtimeVersion);
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入菜品识别缓存失败，不影响本次识别结果：{}", e.getMessage());
        }
    }

    String cacheKey(byte[] imageBytes, String provider, String visionModel, long runtimeVersion) {
        return KEY_PREFIX + cachePart(provider) + ":" + cachePart(visionModel) + ":" + runtimeVersion + ":"
            + imageHash(imageBytes);
    }

    private String imageHash(byte[] imageBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(imageBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private String cachePart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim();
    }

    private void deleteInvalidEntry(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("清理损坏的菜品识别缓存失败：{}", e.getMessage());
        }
    }
}
