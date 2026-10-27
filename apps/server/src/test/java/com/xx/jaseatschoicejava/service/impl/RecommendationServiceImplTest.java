package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.entity.RecommendationLog;
import com.xx.jaseatschoicejava.mapper.RecommendationLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private RecommendationLogMapper recommendationLogMapper;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Test
    void recordFeedback_whenLogMissing_shouldInsertFeedback() {
        when(recommendationLogMapper.selectOne(any())).thenReturn(null);
        when(recommendationLogMapper.insert(any(RecommendationLog.class))).thenReturn(1);

        recommendationService.recordFeedback("user-1", "dish-1", "rec-1", true, false);

        ArgumentCaptor<RecommendationLog> captor = ArgumentCaptor.forClass(RecommendationLog.class);
        verify(recommendationLogMapper).insert(captor.capture());
        RecommendationLog saved = captor.getValue();
        assertEquals("user-1", saved.getUserId());
        assertEquals("dish-1", saved.getDishId());
        assertEquals("rec-1", saved.getRecommendationId());
        assertEquals(true, saved.getIsClicked());
        assertEquals(false, saved.getIsOrdered());
    }

    @Test
    void recordFeedback_whenInsertFails_shouldThrow() {
        when(recommendationLogMapper.selectOne(any())).thenReturn(null);
        when(recommendationLogMapper.insert(any(RecommendationLog.class))).thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> recommendationService.recordFeedback("user-1", "dish-1", "rec-1", true, false));
    }

    @Test
    void recordFeedback_whenUpdateFails_shouldThrow() {
        RecommendationLog existing = new RecommendationLog();
        existing.setId(1L);
        when(recommendationLogMapper.selectOne(any())).thenReturn(existing);
        when(recommendationLogMapper.updateById(any(RecommendationLog.class))).thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> recommendationService.recordFeedback("user-1", "dish-1", "rec-1", true, false));
    }
}
