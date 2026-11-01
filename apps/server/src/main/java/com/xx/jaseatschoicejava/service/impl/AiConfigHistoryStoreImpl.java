package com.xx.jaseatschoicejava.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.AiConfigHistoryEntity;
import com.xx.jaseatschoicejava.mapper.AiConfigHistoryMapper;
import com.xx.jaseatschoicejava.service.AiConfigHistoryStore;

import lombok.extern.slf4j.Slf4j;

/**
 * 将 AI 配置历史保存在专用表中，避免通用系统配置接口读取。
 */
@Slf4j
@Service
public class AiConfigHistoryStoreImpl implements AiConfigHistoryStore {

    private final AiConfigHistoryMapper mapper;

    public AiConfigHistoryStoreImpl(AiConfigHistoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String load() {
        List<AiConfigHistoryEntity> rows = mapper.selectList(new QueryWrapper<AiConfigHistoryEntity>()
            .orderByDesc("created_at", "id")
            .last("LIMIT 1"));
        return rows == null || rows.isEmpty() ? null : rows.get(0).getHistoryJson();
    }

    @Override
    public void save(String historyJson) {
        LocalDateTime now = LocalDateTime.now();
        AiConfigHistoryEntity entity = new AiConfigHistoryEntity();
        entity.setHistoryJson(historyJson);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        mapper.insert(entity);
    }
}
