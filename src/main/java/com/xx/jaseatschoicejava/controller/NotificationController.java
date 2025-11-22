package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Notification;
import com.xx.jaseatschoicejava.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 根据用户ID获取通知列表
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<?> getNotificationsByUserId(@PathVariable Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        queryWrapper.orderByDesc(Notification::getCreateTime); // 按创建时间降序排列
        List<Notification> notifications = notificationService.list(queryWrapper);
        return ResponseResult.success(notifications);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{notificationId}")
    public ResponseResult<?> getNotificationDetail(@PathVariable Long notificationId) {
        Notification notification = notificationService.getById(notificationId);
        if (notification != null) {
            // 标记为已读
            notification.setReadStatus(true);
            notificationService.updateById(notification);
            return ResponseResult.success(notification);
        }
        return ResponseResult.fail("404", "通知不存在");
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public ResponseResult<?> markAsRead(@PathVariable Long notificationId) {
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setReadStatus(true);
        boolean success = notificationService.updateById(notification);
        if (success) {
            return ResponseResult.success("标记成功");
        }
        return ResponseResult.fail("500", "标记失败");
    }
}
