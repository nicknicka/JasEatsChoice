package com.xx.jaseatschoicejava.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.xx.jaseatschoicejava.config.AiQuotaProperties;
import com.xx.jaseatschoicejava.exception.AiQuotaExceededException;

@ExtendWith(MockitoExtension.class)
class AiQuotaServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private AiQuotaProperties properties;
    private AiQuotaService service;
    private final Map<String, Long> counters = new HashMap<>();

    @BeforeEach
    void setUp() {
        properties = new AiQuotaProperties();
        properties.setDishRecognition(new AiQuotaProperties.QuotaLimit(2, Duration.ofHours(1)));
        properties.setRecipe(new AiQuotaProperties.QuotaLimit(1, Duration.ofHours(1)));
        service = new AiQuotaService(redisTemplate, properties);
    }

    @Test
    void consumesUntilLimitAndRejectsTheNextRequest() {
        stubAtomicCounter();
        service.consume("user-1", AiQuotaScope.DISH_RECOGNITION);
        assertDoesNotThrow(() -> service.consume("user-1", AiQuotaScope.DISH_RECOGNITION));

        assertThrows(AiQuotaExceededException.class,
            () -> service.consume("user-1", AiQuotaScope.DISH_RECOGNITION));
    }

    @Test
    void isolatesQuotaByUserAndCapability() {
        stubAtomicCounter();
        service.consume("user-1", AiQuotaScope.DISH_RECOGNITION);
        service.consume("user-2", AiQuotaScope.DISH_RECOGNITION);
        service.consume("user-1", AiQuotaScope.RECIPE);

        assertEquals(1L, counters.get("jaseats:ai:quota:dish-recognition:user-1"));
        assertEquals(1L, counters.get("jaseats:ai:quota:dish-recognition:user-2"));
        assertEquals(1L, counters.get("jaseats:ai:quota:recipe:user-1"));

        assertThrows(AiQuotaExceededException.class,
            () -> service.consume("user-1", AiQuotaScope.RECIPE));
        assertDoesNotThrow(() -> service.consume("user-2", AiQuotaScope.RECIPE));
    }

    @Test
    void passesConfiguredWindowAsMillisecondsToTheAtomicScript() {
        stubAtomicCounter();
        service.consume("user-1", AiQuotaScope.DISH_RECOGNITION);

        verify(redisTemplate).execute(
            any(RedisScript.class),
            org.mockito.ArgumentMatchers.eq(List.of("jaseats:ai:quota:dish-recognition:user-1")),
            org.mockito.ArgumentMatchers.eq("3600000")
        );
    }

    @Test
    void rejectsMissingIdentityInsteadOfCreatingAnAnonymousQuota() {
        assertThrows(IllegalArgumentException.class,
            () -> service.consume("  ", AiQuotaScope.DISH_RECOGNITION));
    }

    private void stubAtomicCounter() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
            .thenAnswer(invocation -> {
                List<String> keys = invocation.getArgument(1);
                return counters.merge(keys.get(0), 1L, Long::sum);
            });
    }
}
