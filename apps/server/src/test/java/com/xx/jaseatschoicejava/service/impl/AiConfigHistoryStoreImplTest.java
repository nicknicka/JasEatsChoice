package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.AiConfigHistoryEntity;
import com.xx.jaseatschoicejava.mapper.AiConfigHistoryMapper;

class AiConfigHistoryStoreImplTest {

    @Test
    void loadsLatestHistory() {
        AiConfigHistoryMapper mapper = mock(AiConfigHistoryMapper.class);
        AiConfigHistoryEntity latest = new AiConfigHistoryEntity(
            "latest", "{\"activeVersion\":2}", LocalDateTime.now(), LocalDateTime.now());
        when(mapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(latest));

        AiConfigHistoryStoreImpl store = new AiConfigHistoryStoreImpl(mapper);

        assertEquals("{\"activeVersion\":2}", store.load());
    }

    @Test
    void savesHistoryWithoutSystemConfigTable() {
        AiConfigHistoryMapper mapper = mock(AiConfigHistoryMapper.class);
        AiConfigHistoryStoreImpl store = new AiConfigHistoryStoreImpl(mapper);

        store.save("{\"activeVersion\":2}");

        ArgumentCaptor<AiConfigHistoryEntity> captor =
            ArgumentCaptor.forClass(AiConfigHistoryEntity.class);
        verify(mapper).insert(captor.capture());
        assertEquals("{\"activeVersion\":2}", captor.getValue().getHistoryJson());
    }
}
