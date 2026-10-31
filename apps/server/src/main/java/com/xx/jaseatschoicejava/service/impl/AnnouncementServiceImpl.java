package com.xx.jaseatschoicejava.service.impl;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.mapper.AnnouncementMapper;
import com.xx.jaseatschoicejava.rag.RagChangeType;
import com.xx.jaseatschoicejava.rag.RagDocumentChangedEvent;
import com.xx.jaseatschoicejava.rag.RagSourceType;
import com.xx.jaseatschoicejava.service.AnnouncementService;

/**
 * 商家公告Service实现类
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final ApplicationEventPublisher eventPublisher;

    public AnnouncementServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public boolean save(Announcement announcement) {
        boolean saved = super.save(announcement);
        if (saved) {
            publishEvent(announcement.getId(), RagChangeType.UPSERT);
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean updateById(Announcement announcement) {
        boolean updated = super.updateById(announcement);
        if (updated) {
            publishEvent(announcement.getId(), RagChangeType.UPSERT);
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean updateBatchById(Collection<Announcement> entityList) {
        boolean success = true;
        for (Announcement announcement : entityList) {
            success &= updateById(announcement);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        boolean removed = super.removeById(id);
        if (removed) {
            publishEvent(String.valueOf(id), RagChangeType.DELETE);
        }
        return removed;
    }

    @Override
    @Transactional
    public boolean removeBatchByIds(Collection<?> list) {
        boolean success = true;
        for (Object id : list) {
            success &= removeById((Serializable) id);
        }
        return success;
    }

    private void publishEvent(String announcementId, RagChangeType changeType) {
        eventPublisher.publishEvent(
            new RagDocumentChangedEvent(RagSourceType.ANNOUNCEMENT, announcementId, changeType));
    }
}
