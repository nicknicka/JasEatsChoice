package com.xx.jaseatschoicejava.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class DishRecognitionCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DishRecognitionCacheService service;

    @BeforeEach
    void setUp() {
        service = new DishRecognitionCacheService(redisTemplate, objectMapper);
    }

    @Test
    void cacheKeyUsesImageHashAndRuntimeModelIdentity() {
        byte[] image = {1, 2, 3};
        String base = service.cacheKey(image, "openai-compatible", "vision-a", 7L);

        assertTrue(base.startsWith("jaseats:ai:dish:v1:openai-compatible:vision-a:7:"));
        assertNotEquals(base, service.cacheKey(new byte[] {1, 2, 4}, "openai-compatible", "vision-a", 7L));
        assertNotEquals(base, service.cacheKey(image, "another-provider", "vision-a", 7L));
        assertNotEquals(base, service.cacheKey(image, "openai-compatible", "vision-b", 7L));
        assertNotEquals(base, service.cacheKey(image, "openai-compatible", "vision-a", 8L));
    }

    @Test
    void findReturnsParsedCachedResult() throws Exception {
        byte[] image = {1, 2, 3};
        String key = service.cacheKey(image, "openai-compatible", "vision-a", 7L);
        Map<String, Object> cached = Map.of("isDish", true, "name", "番茄炒蛋");
        doReturn(valueOperations).when(redisTemplate).opsForValue();
        when(valueOperations.get(key)).thenReturn(objectMapper.writeValueAsString(cached));

        assertEquals(cached, service.find(image, "openai-compatible", "vision-a", 7L).orElseThrow());
    }

    @Test
    void findDeletesMalformedEntryAndFallsBack() throws Exception {
        byte[] image = {1, 2, 3};
        String key = service.cacheKey(image, "openai-compatible", "vision-a", 7L);
        doReturn(valueOperations).when(redisTemplate).opsForValue();
        when(valueOperations.get(key)).thenReturn("not-json");

        assertTrue(service.find(image, "openai-compatible", "vision-a", 7L).isEmpty());
        verify(redisTemplate).delete(key);
    }

    @Test
    void redisReadFailureDegradesWithoutThrowing() {
        byte[] image = {1, 2, 3};
        doReturn(valueOperations).when(redisTemplate).opsForValue();
        when(valueOperations.get(anyString())).thenThrow(new IllegalStateException("redis down"));

        assertDoesNotThrow(() -> service.find(image, "openai-compatible", "vision-a", 7L));
    }

    @Test
    void putWritesJsonWithTwentyFourHourTtl() throws Exception {
        byte[] image = {1, 2, 3};
        String key = service.cacheKey(image, "openai-compatible", "vision-a", 7L);
        Map<String, Object> result = Map.of("isDish", true, "name", "番茄炒蛋");
        doReturn(valueOperations).when(redisTemplate).opsForValue();

        service.put(image, "openai-compatible", "vision-a", 7L, result);

        verify(valueOperations).set(key, objectMapper.writeValueAsString(result), Duration.ofHours(24));
    }

    @Test
    void redisWriteFailureDegradesWithoutThrowing() {
        doReturn(valueOperations).when(redisTemplate).opsForValue();
        org.mockito.Mockito.doThrow(new IllegalStateException("redis down"))
            .when(valueOperations).set(anyString(), anyString(), org.mockito.ArgumentMatchers.any(Duration.class));

        assertDoesNotThrow(() -> service.put(new byte[] {1}, "openai-compatible", "vision-a", 7L,
            Map.of("isDish", true)));
    }

    @Test
    void missingModelIdentityUsesStablePlaceholder() {
        assertTrue(service.cacheKey(new byte[] {1}, null, " ", 7L)
            .startsWith("jaseats:ai:dish:v1:unknown:unknown:7:"));
    }
}
