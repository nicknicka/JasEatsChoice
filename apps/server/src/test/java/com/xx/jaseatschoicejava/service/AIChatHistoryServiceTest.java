package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.entity.AIChatHistory;
import com.xx.jaseatschoicejava.mapper.AIChatHistoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIChatHistoryServiceTest {

    @Mock
    private AIChatHistoryMapper aiChatHistoryMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AIChatHistoryService aiChatHistoryService;

    @Test
    void saveMessage_whenDatabaseInsertFails_shouldReleaseDedupKeyAndThrow() {
        doReturn(valueOperations).when(redisTemplate).opsForValue();
        when(valueOperations.setIfAbsent(
                eq("ai:chat:dedup:merchant:ai:retry-1"), eq("1"), eq(10L), eq(TimeUnit.MINUTES)))
                .thenReturn(true);
        when(aiChatHistoryMapper.insert(any(AIChatHistory.class))).thenThrow(new RuntimeException("database down"));

        assertThrows(RuntimeException.class, () -> aiChatHistoryService.saveMessage(
                "merchant", "ai", "保存失败", "uni_card", "{}", "retry-1"));

        verify(redisTemplate).delete("ai:chat:dedup:merchant:ai:retry-1");
    }
}
