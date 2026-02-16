package com.xx.jaseatschoicejava.util;

import com.xx.jaseatschoicejava.entity.Notification;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 通知工具类
 * <p>
 * 统一管理系统通知的创建，确保通知创建逻辑的一致性和可维护性
 *
 * @Author nickxiao
 * @Date 2026/02/16
 */
@Slf4j
@Component
public class NotificationUtil {

    private static NotificationService notificationService;

    /**
     * 通过Spring注入设置NotificationService
     */
    public NotificationUtil(NotificationService notificationService) {
        NotificationUtil.notificationService = notificationService;
    }

    /**
     * 创建通知（基础方法）
     *
     * @param userId  用户ID
     * @param type    通知类型枚举
     * @param title   通知标题
     * @param content 通知内容
     */
    public static void createNotification(String userId, NotificationTypeEnum type, String title, String content) {
        if (userId == null || userId.isEmpty()) {
            log.warn("用户ID为空，跳过创建通知 - type: {}", type.getCode());
            return;
        }

        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type.getCode());
            notification.setReadStatus(false);
            notification.setSendTime(LocalDateTime.now());
            notification.setCreateTime(LocalDateTime.now());

            notificationService.save(notification);
            log.debug("通知创建成功 - userId: {}, type: {}, title: {}", userId, type.getCode(), title);
        } catch (Exception e) {
            log.error("创建通知失败 - userId: {}, type: {}, title: {}", userId, type.getCode(), title, e);
        }
    }

    /**
     * 创建订单通知
     *
     * @param userId      用户ID
     * @param type        通知类型
     * @param orderId     订单ID
     * @param orderStatus 订单状态描述
     */
    public static void createOrderNotification(String userId, NotificationTypeEnum type, String orderId, String orderStatus) {
        String title = "订单状态更新";
        String content = String.format("您的订单 %s 状态已更新为：%s", orderId, orderStatus);
        createNotification(userId, type, title, content);
    }

    /**
     * 创建群订单通知
     *
     * @param userId      用户ID
     * @param type        通知类型
     * @param groupOrderId 群订单ID
     * @param orderStatus 订单状态描述
     */
    public static void createGroupOrderNotification(String userId, NotificationTypeEnum type, String groupOrderId, String orderStatus) {
        String title = "群订单状态更新";
        String content = String.format("您的群订单 %s 状态已更新为：%s", groupOrderId, orderStatus);
        createNotification(userId, type, title, content);
    }

    /**
     * 创建评价通知
     *
     * @param userId     用户ID
     * @param type       通知类型
     * @param merchantId 商家ID
     * @param orderId    订单ID
     */
    public static void createReviewNotification(String userId, NotificationTypeEnum type, String merchantId, String orderId) {
        String title;
        String content;

        switch (type) {
            case REVIEW_SUBMITTED:
                title = "评价提交成功";
                content = String.format("您已成功提交订单 %s 的评价，感谢您的反馈！", orderId);
                break;
            case REVIEW_REPLY:
                title = "商家已回复评价";
                content = String.format("商家已回复您对订单 %s 的评价，快去看看吧！", orderId);
                break;
            case REVIEW_ADDITIONAL:
                title = "追加评价成功";
                content = "您的追加评价已提交成功";
                break;
            default:
                title = "评价通知";
                content = "您的评价有新动态";
        }

        createNotification(userId, type, title, content);
    }

    /**
     * 创建提现通知
     *
     * @param userId        用户ID
     * @param type          通知类型
     * @param amount        提现金额
     * @param rejectReason  拒绝原因（可选）
     */
    public static void createWithdrawNotification(String userId, NotificationTypeEnum type, String amount, String rejectReason) {
        String title;
        String content;

        switch (type) {
            case WITHDRAW_REQUEST:
                title = "提现申请已提交";
                content = String.format("您的提现申请 %s 元已提交，请等待审核", amount);
                break;
            case WITHDRAW_APPROVED:
                title = "提现审核通过";
                content = String.format("您的提现申请 %s 元已审核通过，正在处理中", amount);
                break;
            case WITHDRAW_REJECTED:
                title = "提现申请被驳回";
                content = String.format("您的提现申请 %s 元已被驳回，原因：%s，金额已退回钱包", amount, rejectReason != null ? rejectReason : "未提供原因");
                break;
            case WITHDRAW_SUCCESS:
                title = "提现已完成";
                content = String.format("您的提现 %s 元已到账，请注意查收", amount);
                break;
            case WITHDRAW_FAILED:
                title = "提现失败";
                content = String.format("您的提现 %s 元失败，原因：%s，金额已退回钱包", amount, rejectReason != null ? rejectReason : "未知原因");
                break;
            default:
                title = "提现通知";
                content = "您的提现有新动态";
        }

        createNotification(userId, type, title, content);
    }

    /**
     * 创建商家审核通知
     *
     * @param merchantId    商家ID
     * @param type          通知类型
     * @param rejectReason  拒绝原因（可选）
     */
    public static void createMerchantAuditNotification(String merchantId, NotificationTypeEnum type, String rejectReason) {
        String title;
        String content;

        if (type == NotificationTypeEnum.MERCHANT_APPROVED) {
            title = "商家注册审核通过";
            content = "恭喜！您的商家注册申请已审核通过，可以开始营业了";
        } else {
            title = "商家注册审核未通过";
            content = String.format("您的商家注册申请未通过审核，原因：%s，请修改后重新提交", rejectReason != null ? rejectReason : "未提供原因");
        }

        createNotification(merchantId, type, title, content);
    }

    /**
     * 创建菜品审核通知
     *
     * @param merchantId    商家ID
     * @param type          通知类型
     * @param dishName      菜品名称
     * @param rejectReason  拒绝原因（可选）
     */
    public static void createDishAuditNotification(String merchantId, NotificationTypeEnum type, String dishName, String rejectReason) {
        String title;
        String content;

        if (type == NotificationTypeEnum.DISH_APPROVED) {
            title = "菜品审核通过";
            content = String.format("您的菜品「%s」已审核通过并自动上架", dishName);
        } else {
            title = "菜品审核未通过";
            content = String.format("您的菜品「%s」审核未通过，原因：%s，请修改后重新提交", dishName, rejectReason != null ? rejectReason : "未提供原因");
        }

        createNotification(merchantId, type, title, content);
    }

    /**
     * 创建加菜通知
     *
     * @param userId        用户ID
     * @param type          通知类型
     * @param userName      用户名称
     * @param rejectReason  拒绝原因（可选）
     */
    public static void createAdditionNotification(String userId, NotificationTypeEnum type, String userName, String rejectReason) {
        String title;
        String content;

        switch (type) {
            case GROUP_ORDER_ADDITION_REQUEST:
                title = "加菜审核";
                content = String.format("用户 %s 发起加菜，请及时审核", userName);
                break;
            case GROUP_ORDER_ADDITION_APPROVED:
                title = "加菜审核通过";
                content = "您的加菜请求已通过审核，等待发起者统一支付";
                break;
            case GROUP_ORDER_ADDITION_REJECTED:
                title = "加菜被驳回";
                content = String.format("您的加菜请求已被驳回：%s", rejectReason != null ? rejectReason : "未提供原因");
                break;
            case GROUP_ORDER_ADDITION_TIMEOUT:
                title = "加菜超时自动驳回";
                content = "您的加菜请求因发起者超时未审核已被自动驳回";
                break;
            case GROUP_ORDER_ADDITION_PAID:
                title = "新加菜订单";
                content = "您的加菜已支付成功，商家将尽快处理";
                break;
            default:
                title = "加菜通知";
                content = "您的加菜有新动态";
        }

        createNotification(userId, type, title, content);
    }

    /**
     * 创建商家新订单通知（用于通知商家有新订单）
     *
     * @param merchantId 商家ID
     * @param orderId    订单ID
     * @param orderType  订单类型（普通订单/群订单）
     */
    public static void createMerchantNewOrderNotification(String merchantId, String orderId, String orderType) {
        String title = "新订单提醒";
        String content = String.format("您有新的%s %s，请及时处理", orderType, orderId);
        createNotification(merchantId, NotificationTypeEnum.ORDER_MERCHANT_ACCEPT, title, content);
    }
}
