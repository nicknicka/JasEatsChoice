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
    public ResponseResult<?> getNotificationsByUserId(@PathVariable String userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        queryWrapper.orderByDesc(Notification::getSendTime); // 按发送时间降序排列
        List<Notification> notifications = notificationService.list(queryWrapper);
        return ResponseResult.success(notifications);
    }

    /**
     * 根据用户ID获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ResponseResult<?> getUnreadCount(@RequestParam String userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        queryWrapper.eq(Notification::getReadStatus, false);
        long count = notificationService.count(queryWrapper);
        return ResponseResult.success((int) count);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{notificationId}")
    public ResponseResult<?> getNotificationDetail(@PathVariable String notificationId) {
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
    public ResponseResult<?> markAsRead(@PathVariable String notificationId) {
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setReadStatus(true);
        boolean success = notificationService.updateById(notification);
        if (success) {
            return ResponseResult.success("标记成功");
        }
        return ResponseResult.fail("500", "标记失败");
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/all-read")
    public ResponseResult<?> markAllAsRead(@RequestParam String userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        queryWrapper.eq(Notification::getReadStatus, false);

        List<Notification> notifications = notificationService.list(queryWrapper);
        for (Notification notification : notifications) {
            notification.setReadStatus(true);
        }

        boolean success = notificationService.updateBatchById(notifications);
        if (success) {
            return ResponseResult.success("所有消息已标记为已读");
        }
        return ResponseResult.fail("500", "标记失败");
    }

    /**
     * 删除单条消息
     */
    @DeleteMapping("/{notificationId}")
    public ResponseResult<?> deleteNotification(@PathVariable String notificationId) {
        boolean success = notificationService.removeById(notificationId);
        if (success) {
            return ResponseResult.success("消息删除成功");
        }
        return ResponseResult.fail("500", "删除失败");
    }

    /**
     * 批量删除消息
     */
    @DeleteMapping("/batch")
    public ResponseResult<?> batchDeleteNotifications(@RequestBody List<String> notificationIds) {
        boolean success = notificationService.removeByIds(notificationIds);
        if (success) {
            return ResponseResult.success("成功删除 " + notificationIds.size() + " 条消息");
        }
        return ResponseResult.fail("500", "批量删除失败");
    }
}
