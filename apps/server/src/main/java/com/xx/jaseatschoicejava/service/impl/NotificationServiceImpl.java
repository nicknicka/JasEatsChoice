package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Notification;
import com.xx.jaseatschoicejava.mapper.NotificationMapper;
import com.xx.jaseatschoicejava.service.NotificationService;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
}
