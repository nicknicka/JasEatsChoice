package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.mapper.AnnouncementMapper;
import com.xx.jaseatschoicejava.service.AnnouncementService;
import org.springframework.stereotype.Service;

/**
 * 商家公告Service实现类
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
}
