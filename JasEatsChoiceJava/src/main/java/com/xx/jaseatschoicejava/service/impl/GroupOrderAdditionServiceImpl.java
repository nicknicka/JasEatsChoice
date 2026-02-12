package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderAddition;
import com.xx.jaseatschoicejava.entity.Notification;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.GroupOrderAdditionMapper;
import com.xx.jaseatschoicejava.service.GroupOrderAdditionService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.NotificationService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 群订单加菜Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupOrderAdditionServiceImpl
        extends ServiceImpl<GroupOrderAdditionMapper, GroupOrderAddition>
        implements GroupOrderAdditionService {

    private final GroupOrderService groupOrderService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderDishService orderDishService;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupOrderAddition requestAddDish(String groupId, String userId, List<Map<String, Object>> dishes) {
        log.info("发起加菜请求 - groupId: {}, userId: {}, 菜品数: {}", groupId, userId, dishes.size());

        // 1. 获取群订单信息
        LambdaQueryWrapper<GroupOrder> groupOrderQuery = new LambdaQueryWrapper<>();
        groupOrderQuery.eq(GroupOrder::getGroupId, groupId);
        groupOrderQuery.in(GroupOrder::getStatus, List.of(0, 1, 2, 3, 4, 5));
        groupOrderQuery.orderByDesc(GroupOrder::getCreateTime);
        groupOrderQuery.last("LIMIT 1");

        GroupOrder groupOrder = groupOrderService.getOne(groupOrderQuery);
        if (groupOrder == null) {
            throw new RuntimeException("未找到进行中的群订单");
        }

        // 2. 计算总金额（简化实现，前端传入已计算好的金额）
        BigDecimal totalAmount = new BigDecimal("0");

        // 3. 创建加菜记录
        GroupOrderAddition addition = new GroupOrderAddition();
        addition.setGroupOrderId(groupOrder.getId());
        addition.setGroupId(groupId);
        addition.setUserId(userId);
        addition.setUserName(getUserName(userId));
        addition.setDishes("{\"dishes\": []}"); // 简化
        addition.setTotalAmount(totalAmount);
        addition.setStatus("pending_review");
        addition.setRequestTime(LocalDateTime.now());

        save(addition);

        // 4. 通知发起者审核
        createNotification(
                groupOrder.getInitiatorId(),
                "加菜审核",
                "用户" + addition.getUserName() + "发起加菜，请及时审核"
        );

        log.info("加菜请求创建成功 - additionId: {}", addition.getId());
        return addition;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewAddition(String additionId, boolean approved, String rejectReason) {
        log.info("审核加菜请求 - additionId: {}, approved: {}", additionId, approved);

        GroupOrderAddition addition = getById(additionId);
        if (addition == null) {
            throw new RuntimeException("加菜记录不存在");
        }

        if (!"pending_review".equals(addition.getStatus())) {
            throw new RuntimeException("该请求已被处理");
        }

        addition.setReviewTime(LocalDateTime.now());

        if (approved) {
            // 审核通过
            addition.setStatus("approved_pending_payment");

            // 通知加菜用户
            createNotification(
                    addition.getUserId(),
                    "加菜审核通过",
                    "您的加菜请求已通过审核，等待发起者统一支付"
            );

            log.info("加菜审核通过 - additionId: {}", additionId);
        } else {
            // 驳回
            addition.setStatus("rejected");
            addition.setRejectReason(rejectReason);

            // 通知加菜用户
            createNotification(
                    addition.getUserId(),
                    "加菜被驳回",
                    "您的加菜请求已被驳回：" + rejectReason
            );

            log.info("加菜审核驳回 - additionId: {}, reason: {}", additionId, rejectReason);
        }

        return updateById(addition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkTimeoutAdditions() {
        log.info("检查超时的加菜请求");

        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);

        LambdaQueryWrapper<GroupOrderAddition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderAddition::getStatus, "pending_review");
        queryWrapper.le(GroupOrderAddition::getRequestTime, timeout);

        List<GroupOrderAddition> timeoutAdditions = list(queryWrapper);

        if (timeoutAdditions.isEmpty()) {
            log.info("没有超时的加菜请求");
            return 0;
        }

        log.info("找到{}条超时的加菜请求", timeoutAdditions.size());

        for (GroupOrderAddition addition : timeoutAdditions) {
            addition.setStatus("rejected");
            addition.setRejectReason("发起者超时未审核，自动驳回");

            // 通知加菜用户
            createNotification(
                    addition.getUserId(),
                    "加菜超时自动驳回",
                    "您的加菜请求因发起者超时未审核已被自动驳回"
            );
        }

        updateBatchById(timeoutAdditions);
        log.info("超时加菜请求处理完成 - 处理数: {}", timeoutAdditions.size());

        return timeoutAdditions.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payAdditionPool(String groupOrderId) {
        log.info("统一支付加菜池 - groupOrderId: {}", groupOrderId);

        // 1. 获取所有待支付的加菜
        LambdaQueryWrapper<GroupOrderAddition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderAddition::getGroupOrderId, groupOrderId);
        queryWrapper.eq(GroupOrderAddition::getStatus, "approved_pending_payment");

        List<GroupOrderAddition> additions = list(queryWrapper);

        if (additions.isEmpty()) {
            log.info("没有待支付的加菜");
            return false;
        }

        // 2. 获取群订单信息
        GroupOrder groupOrder = groupOrderService.getById(groupOrderId);
        if (groupOrder == null) {
            throw new RuntimeException("群订单不存在");
        }

        // 3. 为每个加菜创建订单
        for (GroupOrderAddition addition : additions) {
            Order additionOrder = new Order();
            additionOrder.setUserId(addition.getUserId());
            additionOrder.setMerchantId(groupOrder.getMerchantId());
            if (addition.getTotalAmount() != null) {
                additionOrder.setTotalAmount(addition.getTotalAmount());
                additionOrder.setPaidAmount(addition.getTotalAmount());
            }
            additionOrder.setAddressId(groupOrder.getAddressId());
            additionOrder.setRemark("群订单加菜");
            additionOrder.setStatus(1); // 待接单
            additionOrder.setCreateTime(LocalDateTime.now());
            additionOrder.setUpdateTime(LocalDateTime.now());

            orderService.save(additionOrder);

            // 更新加菜记录
            addition.setStatus("paid");
            addition.setRelatedOrderId(additionOrder.getId());
            addition.setPayTime(LocalDateTime.now());
        }

        updateBatchById(additions);

        // 4. 通知商家
        createNotification(
                groupOrder.getMerchantId(),
                "新加菜订单",
                "群订单有" + additions.size() + "个加菜已支付，请及时处理"
        );

        log.info("加菜池支付成功 - groupOrderId: {}, 加菜数: {}", groupOrderId, additions.size());

        return true;
    }

    @Override
    public List<GroupOrderAddition> getPendingAdditions(String groupOrderId) {
        LambdaQueryWrapper<GroupOrderAddition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderAddition::getGroupOrderId, groupOrderId);
        queryWrapper.eq(GroupOrderAddition::getStatus, "pending_review");
        queryWrapper.orderByAsc(GroupOrderAddition::getRequestTime);

        return list(queryWrapper);
    }

    @Override
    public List<GroupOrderAddition> getApprovedPendingPayments(String groupOrderId) {
        LambdaQueryWrapper<GroupOrderAddition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderAddition::getGroupOrderId, groupOrderId);
        queryWrapper.eq(GroupOrderAddition::getStatus, "approved_pending_payment");
        queryWrapper.orderByAsc(GroupOrderAddition::getRequestTime);

        return list(queryWrapper);
    }

    @Override
    public BigDecimal calculateTotalAmount(List<Map<String, Object>> dishes) {
        BigDecimal total = BigDecimal.ZERO;
        // TODO: 实现从菜品ID获取价格并计算总额
        // 简化实现，返回0
        return total;
    }

    /**
     * 获取用户名称
     */
    private String getUserName(String userId) {
        try {
            User user = userService.getById(userId);
            return user != null ? user.getNickname() : "未知用户";
        } catch (Exception e) {
            log.error("获取用户名称失败 - userId: {}", userId, e);
            return "未知用户";
        }
    }

    /**
     * 创建通知
     */
    private void createNotification(String userId, String title, String content) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("group_order_addition");
            notification.setReadStatus(false);
            notification.setSendTime(LocalDateTime.now());
            notificationService.save(notification);
        } catch (Exception e) {
            log.error("创建通知失败", e);
        }
    }
}
