package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.GroupChatService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 群订单控制器
 */
@RestController
@RequestMapping("/v1/group-orders")
public class GroupOrderController {

    private static final Logger logger = LoggerFactory.getLogger(GroupOrderController.class);

    @Autowired
    private GroupOrderService groupOrderService;

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private PaymentService paymentService;

    /**
     * 获取或创建群组的草稿订单
     * ⭐ 用于前端恢复未完成的群订单
     */
    @GetMapping("/groups/{groupId}/draft-order")
    public ResponseResult<?> getOrCreateDraftOrder(@PathVariable String groupId,
                                                   @RequestParam String initiatorId) {
        try {
            // 查询该群的草稿订单
            LambdaQueryWrapper<GroupOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupOrder::getGroupId, groupId);
            queryWrapper.eq(GroupOrder::getInitiatorId, initiatorId);
            queryWrapper.eq(GroupOrder::getStatus, -1); // 草稿状态
            queryWrapper.orderByDesc(GroupOrder::getCreateTime);
            queryWrapper.last("LIMIT 1");

            GroupOrder draftOrder = groupOrderService.getOne(queryWrapper);

            if (draftOrder != null) {
                // 返回已有的草稿订单
                return ResponseResult.success(draftOrder);
            } else {
                // 创建新的草稿订单
                GroupOrder newDraftOrder = new GroupOrder();
                newDraftOrder.setInitiatorId(initiatorId);
                newDraftOrder.setGroupId(groupId);
                newDraftOrder.setStatus(-1); // 草稿状态
                newDraftOrder.setCreateTime(LocalDateTime.now());
                newDraftOrder.setUpdateTime(LocalDateTime.now());

                boolean saved = groupOrderService.save(newDraftOrder);
                if (saved) {
                    return ResponseResult.success(newDraftOrder);
                } else {
                    return ResponseResult.fail("500", "创建草稿订单失败");
                }
            }
        } catch (Exception e) {
            logger.error("获取或创建草稿订单失败", e);
            return ResponseResult.fail("500", "获取或创建草稿订单失败：" + e.getMessage());
        }
    }

    /**
     * 创建群订单
     * ⭐ 支持两种模式：
     * 1. 完整模式：提供所有信息立即创建订单
     * 2. 初始模式：仅提供基本信息，创建初始状态订单（status=-1 表示草稿）
     */
    @PostMapping("/group-orders")
    public ResponseResult<?> createGroupOrder(@RequestBody Map<String, Object> request) {
        try {
            // 解析群订单信息
            GroupOrder groupOrder = new GroupOrder();
            groupOrder.setInitiatorId(request.get("initiatorId").toString());
            groupOrder.setGroupId(request.get("groupId").toString());

            // ⭐ 支持初始模式：可选字段
            if (request.containsKey("merchantId") && request.get("merchantId") != null) {
                groupOrder.setMerchantId(request.get("merchantId").toString());
            }
            if (request.containsKey("addressId") && request.get("addressId") != null) {
                groupOrder.setAddressId(request.get("addressId").toString());
            }
            groupOrder.setRemark((String) request.get("remark"));

            // ⭐ 如果没有提供商家ID，设置为草稿状态
            if (groupOrder.getMerchantId() == null || groupOrder.getMerchantId().isEmpty()) {
                groupOrder.setStatus(-1); // 草稿状态
            } else {
                groupOrder.setStatus(0); // 待支付状态
            }

            // 解析菜品列表 - 添加@SuppressWarnings消除未检查转换警告
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dishItemsMap = (List<Map<String, Object>>) request.get("dishItems");
            List<GroupOrderDish> dishItems = dishItemsMap.stream()
                    .map(item -> {
                        GroupOrderDish dish = new GroupOrderDish();
                        dish.setDishId(item.get("dishId").toString());
                        dish.setQuantity(Integer.valueOf(item.get("quantity").toString()));
                        dish.setCustomization((String) item.get("customization"));
                        dish.setUserId(item.get("userId").toString());
                        return dish;
                    }).toList();

            // 创建群订单
            boolean success = groupOrderService.createGroupOrder(groupOrder, dishItems);
            if (success) {
                return ResponseResult.success(groupOrder.getId()); // 返回群订单ID
            }
            return ResponseResult.fail("500", "创建群订单失败");
        } catch (Exception e) {
            logger.error("创建群订单失败", e);
            return ResponseResult.fail("500", "创建群订单失败：" + e.getMessage());
        }
    }

    /**
     * 获取群订单列表
     */
    @GetMapping("/groups/{groupId}/orders")
    public ResponseResult<?> getGroupOrders(@PathVariable String groupId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        List<GroupOrder> groupOrders = groupOrderService.getGroupOrdersByGroupId(groupId, status, page, size);
        // 分页和状态筛选已实现
        return ResponseResult.success(groupOrders);
    }

    /**
     * 获取群订单详情
     */
    @GetMapping("/group-orders/{groupOrderId}")
    public ResponseResult<?> getGroupOrderDetail(@PathVariable String groupOrderId) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder != null) {
            // 获取菜品列表
            List<GroupOrderDish> dishItems = groupOrderService.getGroupOrderDishes(groupOrderId);
            // 构建返回结果
            return ResponseResult.success(Map.of(
                    "groupOrder", groupOrder,
                    "dishItems", dishItems
            ));
        }
        return ResponseResult.fail("404", "群订单不存在");
    }

    /**
     * 同步群订单消息
     */
    @PostMapping("/group-orders/{groupOrderId}/sync-message")
    public ResponseResult<?> syncGroupOrderMessage(@PathVariable String groupOrderId, @RequestBody Map<String, Object> params) {
        // 获取消息内容
        String message = (String) params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseResult.fail("400", "请提供要同步的消息内容");
        }

        // 根据PRD要求：同步内容以「【订单同步】」开头
        String syncedMessage = "【订单同步】" + message;

        // 获取订单信息以获取群ID
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }

        // 调用群聊消息发送服务
        boolean sendSuccess = groupChatService.sendMessage(groupOrder.getGroupId(), syncedMessage);

        // 构建返回结果
        return ResponseResult.success(Map.of(
                "groupOrderId", groupOrderId,
                "originalMessage", message,
                "syncedMessage", syncedMessage,
                "sendStatus", sendSuccess ? "success" : "failed",
                "status", "synced"
        ));
    }

    /**
     * 删除群订单（取消群订单）
     * ⭐ 支持取消草稿和已支付但未接单的订单
     */
    @DeleteMapping("/group-orders/{groupOrderId}")
    public ResponseResult<?> cancelGroupOrder(@PathVariable String groupOrderId) {
        try {
            // 查询订单
            GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
            if (groupOrder == null) {
                return ResponseResult.fail("404", "群订单不存在");
            }

            Integer status = groupOrder.getStatus();

            // 检查订单状态：只允许取消草稿(-1)、待支付(0)、待接单(1)的订单
            if (status < -1 || status > 1) {
                return ResponseResult.fail("400", "只能取消待接单之前的订单（草稿/待支付/待接单）");
            }

            // 如果订单已支付（status >= 0），需要退款
            if (status >= 0) {
                try {
                    // 查询支付记录
                    PaymentRecord paymentRecord = paymentService.getPaymentByOrderId(groupOrderId);
                    if (paymentRecord != null && paymentRecord.getAmount() != null) {
                        // 调用退款服务
                        boolean refundSuccess = paymentService.refundPayment(
                            groupOrderId,
                            paymentRecord.getAmount(),
                            "群订单取消"
                        );

                        if (!refundSuccess) {
                            return ResponseResult.fail("500", "退款失败，无法取消订单");
                        }

                        logger.info("群订单退款成功 - orderId: {}, 退款金额: {}",
                            groupOrderId, paymentRecord.getAmount());
                    }
                } catch (Exception e) {
                    logger.error("群订单退款失败 - orderId: {}", groupOrderId, e);
                    return ResponseResult.fail("500", "退款失败：" + e.getMessage());
                }
            }

            // 更新订单状态为已取消(4)
            groupOrder.setStatus(4);
            groupOrder.setUpdateTime(LocalDateTime.now());
            boolean updated = groupOrderService.updateById(groupOrder);

            if (updated) {
                logger.info("取消群订单成功 - orderId: {}, groupId: {}, 原状态: {}",
                    groupOrderId, groupOrder.getGroupId(), status);

                // 通知发起者订单已取消
                NotificationUtil.createGroupOrderNotification(
                    groupOrder.getInitiatorId(),
                    NotificationTypeEnum.GROUP_ORDER_CANCELLED,
                    groupOrderId,
                    "已取消"
                );

                // 通知商家订单已取消
                NotificationUtil.createGroupOrderNotification(
                    groupOrder.getMerchantId(),
                    NotificationTypeEnum.GROUP_ORDER_CANCELLED,
                    groupOrderId,
                    "已取消"
                );

                return ResponseResult.success(status >= 0 ? "订单已取消并退款" : "订单已取消");
            } else {
                return ResponseResult.fail("500", "取消订单失败");
            }
        } catch (Exception e) {
            logger.error("取消群订单失败", e);
            return ResponseResult.fail("500", "取消订单失败：" + e.getMessage());
        }
    }

    /**
     * 更新群订单状态
     * ⭐ 用于支付成功后更新订单状态
     */
    @PutMapping("/group-orders/{groupOrderId}/status")
    public ResponseResult<?> updateGroupOrderStatus(
            @PathVariable String groupOrderId,
            @RequestBody Map<String, Object> params) {
        try {
            // 查询订单
            GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
            if (groupOrder == null) {
                return ResponseResult.fail("404", "群订单不存在");
            }

            // 获取新状态
            Integer newStatus = Integer.valueOf(params.get("status").toString());

            // 状态验证：-1=草稿, 0=待支付, 1=待接单(已支付), 2-4=处理中, 5=已完成, 6=已取消
            if (newStatus < -1 || newStatus > 6) {
                return ResponseResult.fail("400", "无效的状态值");
            }

            // 记录旧状态用于日志
            Integer oldStatus = groupOrder.getStatus();

            // 更新状态
            groupOrder.setStatus(newStatus);
            groupOrder.setUpdateTime(LocalDateTime.now());

            // 如果提供了总金额，也更新总金额
            if (params.containsKey("totalAmount")) {
                groupOrder.setTotalAmount(Double.valueOf(params.get("totalAmount").toString()));
            }

            // 保存更新
            boolean updated = groupOrderService.updateById(groupOrder);
            if (updated) {
                logger.info("更新群订单状态成功 - orderId: {}, status: {} -> {}",
                        groupOrderId, oldStatus, newStatus);

                // 根据新状态发送通知
                switch (newStatus) {
                    case 1: // 待接单（已支付）
                        NotificationUtil.createGroupOrderNotification(
                            groupOrder.getInitiatorId(),
                            NotificationTypeEnum.GROUP_ORDER_PAYMENT_SUCCESS,
                            groupOrderId,
                            "待接单"
                        );
                        NotificationUtil.createMerchantNewOrderNotification(
                            groupOrder.getMerchantId(),
                            groupOrderId,
                            "群订单"
                        );
                        break;
                    case 2: // 备菜中
                        NotificationUtil.createGroupOrderNotification(
                            groupOrder.getInitiatorId(),
                            NotificationTypeEnum.GROUP_ORDER_PREPARING_COMPLETE,
                            groupOrderId,
                            "备菜中"
                        );
                        break;
                    case 3: // 烹饪中
                        NotificationUtil.createGroupOrderNotification(
                            groupOrder.getInitiatorId(),
                            NotificationTypeEnum.GROUP_ORDER_COOKING_COMPLETE,
                            groupOrderId,
                            "烹饪中"
                        );
                        break;
                    case 4: // 待上菜
                        NotificationUtil.createGroupOrderNotification(
                            groupOrder.getInitiatorId(),
                            NotificationTypeEnum.GROUP_ORDER_WAITING_SERVING,
                            groupOrderId,
                            "待上菜"
                        );
                        break;
                    case 5: // 已完成
                        NotificationUtil.createGroupOrderNotification(
                            groupOrder.getInitiatorId(),
                            NotificationTypeEnum.GROUP_ORDER_COMPLETE,
                            groupOrderId,
                            "已完成"
                        );
                        break;
                }

                return ResponseResult.success(Map.of(
                        "groupOrderId", groupOrderId,
                        "status", newStatus,
                        "message", "状态更新成功"
                ));
            } else {
                return ResponseResult.fail("500", "更新状态失败");
            }
        } catch (Exception e) {
            logger.error("更新群订单状态失败", e);
            return ResponseResult.fail("500", "更新状态失败：" + e.getMessage());
        }
    }
}
