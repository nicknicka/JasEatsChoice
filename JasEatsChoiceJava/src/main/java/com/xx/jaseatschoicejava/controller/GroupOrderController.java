package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.entity.UserCoupon;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.AlipayPayService;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.GroupChatService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.GroupService;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.UserCouponService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.service.WechatPayService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.math.BigDecimal;

/**
 * 群订单控制器
 */
@RestController
@RequestMapping("/v1/group-orders")
public class GroupOrderController {

    private static final Logger logger = LoggerFactory.getLogger(GroupOrderController.class);
    private static final String GROUP_ORDER_CONFIRMED_PREFIX = "[GROUP_ORDER_CONFIRMED]";

    @Autowired
    private GroupOrderService groupOrderService;

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private DishService dishService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired
    private AlipayPayService alipayPayService;

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
                groupOrderService.ensureGroupOrderMember(draftOrder.getId(), initiatorId, "initiator", null);
                draftOrder = groupOrderService.getGroupOrderDetail(draftOrder.getId());
                if (draftOrder != null && (draftOrder.getMaxParticipants() == null || draftOrder.getMaxParticipants() <= 0)) {
                    draftOrder.setMaxParticipants(Math.max(1, groupOrderService.getActiveGroupOrderMembers(draftOrder.getId()).size()));
                }
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
                    groupOrderService.ensureGroupOrderMember(newDraftOrder.getId(), initiatorId, "initiator", null);
                    GroupOrder draftOrderDetail = groupOrderService.getGroupOrderDetail(newDraftOrder.getId());
                    if (draftOrderDetail != null && (draftOrderDetail.getMaxParticipants() == null || draftOrderDetail.getMaxParticipants() <= 0)) {
                        draftOrderDetail.setMaxParticipants(1);
                    }
                    return ResponseResult.success(draftOrderDetail == null ? newDraftOrder : draftOrderDetail);
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
            if (request.containsKey("maxParticipants") && request.get("maxParticipants") != null) {
                groupOrder.setMaxParticipants(Integer.valueOf(request.get("maxParticipants").toString()));
            }

            // ⭐ 如果没有提供商家ID，设置为草稿状态
            if (groupOrder.getMerchantId() == null || groupOrder.getMerchantId().isEmpty()) {
                groupOrder.setStatus(-1); // 草稿状态
            } else {
                groupOrder.setStatus(0); // 待支付状态
            }

            // 解析菜品列表 - 添加@SuppressWarnings消除未检查转换警告
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dishItemsMap = (List<Map<String, Object>>) request.get("dishItems");
            List<GroupOrderDish> dishItems = dishItemsMap == null ? new ArrayList<>() : dishItemsMap.stream()
                    .map(item -> {
                        GroupOrderDish dish = new GroupOrderDish();
                        dish.setDishId(item.get("dishId").toString());
                        dish.setQuantity(Integer.valueOf(item.get("quantity").toString()));
                        dish.setCustomization((String) item.get("customization"));
                        Object userId = item.get("userId") != null ? item.get("userId") : request.get("initiatorId");
                        dish.setUserId(userId == null ? null : userId.toString());
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
        return ResponseResult.success(groupOrders.stream().map(this::buildGroupOrderSummary).toList());
    }

    /**
     * 获取用户参与或发起的拼单列表
     */
    @GetMapping("/users/{userId}/orders")
    public ResponseResult<?> getUserGroupOrders(@PathVariable String userId,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        List<GroupOrder> groupOrders = groupOrderService.getUserGroupOrders(userId, status, page, size);
        return ResponseResult.success(groupOrders.stream().map(this::buildGroupOrderSummary).toList());
    }

    /**
     * 获取群订单详情
     */
    @GetMapping("/group-orders/{groupOrderId}")
    public ResponseResult<?> getGroupOrderDetail(@PathVariable String groupOrderId,
                                                 @RequestParam(required = false) String userId) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder != null) {
            Map<String, Object> runtimeState = groupOrderService.getGroupOrderRuntimeState(groupOrder, userId);
            // 获取菜品列表
            List<Map<String, Object>> dishItems = groupOrderService.getGroupOrderDishes(groupOrderId).stream()
                    .map(item -> buildDishItemDetail(item, runtimeState))
                    .toList();
            // 构建返回结果
            return ResponseResult.success(Map.of(
                    "groupOrder", buildGroupOrderPayload(groupOrder, runtimeState, userId),
                    "dishItems", dishItems
            ));
        }
        return ResponseResult.fail("404", "群订单不存在");
    }

    /**
     * 按订单码加入拼单
     */
    @PostMapping("/join")
    public ResponseResult<?> joinByOrderCode(@RequestBody Map<String, Object> params) {
        Object orderCodeValue = params.get("orderCode");
        Object userIdValue = params.get("userId");
        if (orderCodeValue == null || orderCodeValue.toString().isBlank()) {
            return ResponseResult.fail("400", "orderCode不能为空");
        }
        if (userIdValue == null || userIdValue.toString().isBlank()) {
            return ResponseResult.fail("400", "userId不能为空");
        }

        Map<String, Object> result = groupOrderService.joinByOrderCode(orderCodeValue.toString(), userIdValue.toString());
        if (result == null) {
            return ResponseResult.fail("404", "未找到对应的拼单订单");
        }

        Integer status = (Integer) result.get("status");
        if (status != null && status >= 5) {
            return ResponseResult.fail("400", "当前拼单已结束，无法加入");
        }
        boolean joined = Boolean.TRUE.equals(result.get("joined"));
        if (!joined && Boolean.TRUE.equals(result.get("locked"))) {
            return ResponseResult.fail("400", "当前拼单已确认成团，暂不支持继续加入");
        }
        if (!joined && Boolean.FALSE.equals(result.get("joinable"))) {
            return ResponseResult.fail("400", String.valueOf(result.getOrDefault("message", "当前拼单人数已满，无法加入")));
        }
        return ResponseResult.success(result, "加入成功");
    }

    /**
     * 获取用户选菜记录
     */
    @GetMapping("/group-orders/{groupOrderId}/selections/{userId}")
    public ResponseResult<?> getUserSelections(@PathVariable String groupOrderId, @PathVariable String userId) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }
        return ResponseResult.success(groupOrderService.getUserSelections(groupOrderId, userId));
    }

    /**
     * 保存用户选菜记录
     */
    @PostMapping("/group-orders/{groupOrderId}/selections")
    public ResponseResult<?> saveUserSelections(@PathVariable String groupOrderId,
                                                @RequestBody Map<String, Object> params) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }

        Object userIdValue = params.get("userId");
        if (userIdValue == null || userIdValue.toString().isBlank()) {
            return ResponseResult.fail("400", "userId不能为空");
        }

        Map<String, Object> runtimeState = groupOrderService.getGroupOrderRuntimeState(groupOrder, userIdValue.toString());
        if (Boolean.TRUE.equals(runtimeState.get("locked"))) {
            return ResponseResult.fail("400", "当前拼单已确认成团，不能继续修改选菜");
        }

        String merchantId = getStringValue(params.get("merchantId"));
        String addressId = getStringValue(params.get("addressId"));
        String remark = getStringValue(params.get("remark"));

        boolean draftUpdated = false;
        if (merchantId != null && !merchantId.isBlank() && !Objects.equals(groupOrder.getMerchantId(), merchantId)) {
            groupOrder.setMerchantId(merchantId);
            draftUpdated = true;
        }
        if (addressId != null && !addressId.isBlank() && !Objects.equals(groupOrder.getAddressId(), addressId)) {
            groupOrder.setAddressId(addressId);
            draftUpdated = true;
        }
        if (remark != null && !Objects.equals(groupOrder.getRemark(), remark)) {
            groupOrder.setRemark(remark);
            draftUpdated = true;
        }
        if (groupOrder.getStatus() != null && groupOrder.getStatus() == -1
                && groupOrder.getMerchantId() != null && !groupOrder.getMerchantId().isBlank()) {
            groupOrder.setStatus(0);
            draftUpdated = true;
        }
        if (draftUpdated) {
            groupOrder.setUpdateTime(LocalDateTime.now());
            groupOrderService.updateById(groupOrder);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dishes = (List<Map<String, Object>>) params.get("dishes");
        try {
            List<Map<String, Object>> saved = groupOrderService.saveUserSelections(
                    groupOrderId,
                    userIdValue.toString(),
                    dishes == null ? new ArrayList<>() : dishes
            );
            return ResponseResult.success(saved, "保存成功");
        } catch (IllegalStateException e) {
            return ResponseResult.fail("400", e.getMessage());
        }
    }

    /**
     * 获取结算信息
     */
    @GetMapping("/group-orders/{groupOrderId}/settlement")
    public ResponseResult<?> getSettlement(@PathVariable String groupOrderId,
                                           @RequestParam String userId) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }
        return ResponseResult.success(groupOrderService.getSettlement(groupOrderId, userId));
    }

    /**
     * 确认成团
     */
    @PostMapping("/group-orders/{groupOrderId}/confirm")
    public ResponseResult<?> confirmGroupOrder(@PathVariable String groupOrderId,
                                               @RequestBody(required = false) Map<String, Object> params) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }

        String userId = params == null ? null : getStringValue(params.get("userId"));
        if (userId == null || userId.isBlank()) {
            return ResponseResult.fail("400", "userId不能为空");
        }
        if (!userId.equals(groupOrder.getInitiatorId())) {
            return ResponseResult.fail("403", "只有发起人可以确认成团");
        }
        if (groupOrder.getStatus() == null || groupOrder.getStatus() != 0) {
            return ResponseResult.fail("400", "当前拼单状态不支持确认成团");
        }
        Map<String, Object> runtimeState = groupOrderService.getGroupOrderRuntimeState(groupOrder, userId);
        if (Boolean.TRUE.equals(runtimeState.get("locked"))) {
            return ResponseResult.fail("400", "当前拼单已确认成团，无需重复确认");
        }

        List<GroupOrderDish> dishItems = groupOrderService.getGroupOrderDishes(groupOrderId);
        if (dishItems.isEmpty()) {
            return ResponseResult.fail("400", "至少选择一份菜品后才能确认成团");
        }
        if (!Boolean.TRUE.equals(runtimeState.get("canConfirm"))) {
            return ResponseResult.fail("400", "当前拼单状态不支持确认成团");
        }

        GroupOrder confirmedGroupOrder = groupOrderService.confirmGroupOrder(groupOrderId);
        Map<String, Object> confirmedRuntimeState = groupOrderService.getGroupOrderRuntimeState(confirmedGroupOrder, userId);
        return ResponseResult.success(buildGroupOrderPayload(confirmedGroupOrder, confirmedRuntimeState, userId), "确认成团成功");
    }

    /**
     * 获取拼单二维码内容
     */
    @GetMapping("/group-orders/{groupOrderId}/qrcode")
    public ResponseResult<?> getGroupOrderQRCode(@PathVariable String groupOrderId) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }

        String digits = groupOrderId.replaceAll("\\D", "");
        String orderCode = digits.length() > 6 ? digits.substring(digits.length() - 6) : String.format("%6s", digits).replace(' ', '0');
        return ResponseResult.success(Map.of(
                "groupOrderId", groupOrderId,
                "orderCode", orderCode,
                "qrcodeContent", String.format("{\"type\":\"group_order\",\"orderId\":\"%s\",\"orderCode\":\"%s\"}", groupOrderId, orderCode)
        ));
    }

    /**
     * 创建群订单支付
     */
    @PostMapping("/group-orders/{groupOrderId}/pay")
    public ResponseResult<?> payGroupOrder(@PathVariable String groupOrderId,
                                           @RequestBody Map<String, Object> params) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }

        String userId = getStringValue(params.get("userId"));
        String paymentType = getStringValue(params.get("paymentType"));
        String paymentMethod = getStringValue(params.get("paymentMethod"));
        String couponId = getStringValue(params.get("couponId"));
        String paymentPassword = getStringValue(params.get("paymentPassword"));

        if (userId == null || userId.isBlank()) {
            return ResponseResult.fail("400", "userId不能为空");
        }

        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "balance";
        }
        if (paymentType == null || paymentType.isBlank()) {
            paymentType = "single";
        }
        Map<String, Object> runtimeState = groupOrderService.getGroupOrderRuntimeState(groupOrder, userId);
        if (!Boolean.TRUE.equals(runtimeState.get("currentUserJoined"))) {
            return ResponseResult.fail("403", "当前用户未加入该拼单");
        }
        if (groupOrder.getStatus() != null && groupOrder.getStatus() == 0 && !Boolean.TRUE.equals(runtimeState.get("locked"))) {
            return ResponseResult.fail("400", "请先确认成团后再支付");
        }

        if (groupOrder.getStatus() != null && groupOrder.getStatus() >= 5) {
            return ResponseResult.fail("400", "当前群订单已结束，无法继续支付");
        }
        if ("single".equals(paymentType) && Boolean.TRUE.equals(runtimeState.get("currentUserPaid"))) {
            return ResponseResult.fail("400", "当前用户已完成支付，无需重复支付");
        }
        if (Boolean.TRUE.equals(runtimeState.get("paidByAll"))) {
            return ResponseResult.fail("400", "当前群订单已全部支付完成");
        }

        String normalizedPaymentMethod = "balance".equals(paymentMethod) ? "wallet" : paymentMethod;
        BigDecimal payableAmount = calculatePayableAmount(groupOrderId, userId, paymentType);
        if (payableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseResult.fail("400", "当前没有可支付的拼单金额");
        }

        boolean couponUsed = false;
        try {
            if (couponId != null && !couponId.isBlank()) {
                if (!userCouponService.checkCouponAvailable(couponId, payableAmount)) {
                    return ResponseResult.fail("400", "优惠券不可用");
                }

                UserCoupon coupon = userCouponService.getById(couponId);
                if (coupon != null && coupon.getAmount() != null) {
                    payableAmount = payableAmount.subtract(coupon.getAmount());
                    if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
                        payableAmount = BigDecimal.ZERO;
                    }
                }

                couponUsed = userCouponService.useCoupon(couponId, groupOrderId);
            }

            PaymentRecord paymentRecord = paymentService.createPayment(
                    groupOrderId,
                    userId,
                    groupOrder.getMerchantId(),
                    payableAmount,
                    normalizedPaymentMethod
            );
            paymentRecord.setRemark(paymentType);
            paymentService.updateById(paymentRecord);

            if ("wallet".equals(normalizedPaymentMethod)) {
                handleGroupOrderWalletPayment(groupOrder, paymentRecord, paymentType, paymentPassword);
                return ResponseResult.success(buildPaymentResponse(paymentRecord, paymentMethod, Map.of(
                        "type", "balance",
                        "status", "success"
                )), "支付成功");
            }

            if ("wechat".equals(normalizedPaymentMethod)) {
                Map<String, Object> payResult = wechatPayService.createMiniPayOrder(
                        paymentRecord.getPaymentNo(),
                        payableAmount,
                        "群订单支付-" + groupOrderId,
                        ""
                );
                Map<String, Object> paymentParams = new HashMap<>(payResult);
                paymentParams.put("type", "wechat");
                return ResponseResult.success(buildPaymentResponse(paymentRecord, paymentMethod, paymentParams), "支付单创建成功");
            }

            if ("alipay".equals(normalizedPaymentMethod)) {
                Map<String, Object> payResult = alipayPayService.createWapPayOrder(
                        paymentRecord.getPaymentNo(),
                        payableAmount,
                        "群订单支付-" + groupOrderId
                );
                Map<String, Object> paymentParams = new HashMap<>(payResult);
                paymentParams.put("type", "alipay");
                return ResponseResult.success(buildPaymentResponse(paymentRecord, paymentMethod, paymentParams), "支付单创建成功");
            }

            return ResponseResult.fail("400", "暂不支持该支付方式");
        } catch (Exception e) {
            logger.error("群订单支付失败 - groupOrderId: {}", groupOrderId, e);
            if (couponUsed && couponId != null && !couponId.isBlank()) {
                userCouponService.releaseCoupon(couponId, groupOrderId);
            }
            return ResponseResult.fail("500", "支付失败：" + e.getMessage());
        }
    }

    /**
     * 退出拼单
     */
    @DeleteMapping("/group-orders/{groupOrderId}/leave")
    public ResponseResult<?> leaveGroupOrder(@PathVariable String groupOrderId,
                                             @RequestParam String userId) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }

        if (groupOrder.getInitiatorId() != null && groupOrder.getInitiatorId().equals(userId)) {
            return ResponseResult.fail("400", "发起人不能直接退出拼单，请取消群订单");
        }

        Map<String, Object> runtimeState = groupOrderService.getGroupOrderRuntimeState(groupOrder, userId);
        if (!Boolean.TRUE.equals(runtimeState.get("currentUserJoined"))) {
            return ResponseResult.fail("404", "当前用户未加入该拼单");
        }

        Integer status = groupOrder.getStatus();
        if (status != null && status > 0) {
            return ResponseResult.fail("400", "当前拼单状态不支持退出");
        }
        if (Boolean.TRUE.equals(runtimeState.get("locked"))) {
            return ResponseResult.fail("400", "当前拼单已确认成团，不能退出");
        }
        if (Boolean.TRUE.equals(runtimeState.get("currentUserPaid"))) {
            return ResponseResult.fail("400", "已支付成员暂不支持退出拼单");
        }

        boolean success = groupOrderService.leaveGroupOrder(groupOrderId, userId);
        if (!success) {
            return ResponseResult.fail("404", "当前用户未加入该拼单");
        }

        return ResponseResult.success(Map.of(
                "groupOrderId", groupOrderId,
                "userId", userId,
                "message", "退出拼单成功"
        ));
    }

    /**
     * 邀请好友加入拼单
     */
    @PostMapping("/group-orders/{groupOrderId}/invite")
    public ResponseResult<?> inviteGroupOrder(@PathVariable String groupOrderId,
                                              @RequestBody(required = false) Map<String, Object> params) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder == null) {
            return ResponseResult.fail("404", "群订单不存在");
        }

        Group group = groupOrder.getGroupId() == null ? null : groupService.getById(groupOrder.getGroupId());
        @SuppressWarnings("unchecked")
        List<String> friendIds = params == null ? List.of() : (List<String>) params.getOrDefault("friendIds", List.of());
        String orderCode = buildOrderCode(groupOrderId);

        return ResponseResult.success(Map.of(
                "groupOrderId", groupOrderId,
                "groupId", groupOrder.getGroupId() == null ? "" : groupOrder.getGroupId(),
                "orderCode", orderCode,
                "inviteeCount", friendIds.size(),
                "shareTitle", "加入我的群订单「" + (group == null || group.getGroupName() == null ? orderCode : group.getGroupName()) + "」",
                "shareText", "使用订单码 " + orderCode + " 参与拼单",
                "qrcodeContent", String.format("{\"type\":\"group_order\",\"orderId\":\"%s\",\"orderCode\":\"%s\"}", groupOrderId, orderCode)
        ));
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

            // 更新订单状态为已取消(6)
            groupOrder.setStatus(6);
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

    private Map<String, Object> buildDishItemDetail(GroupOrderDish dishItem, Map<String, Object> runtimeState) {
        Dish dish = dishService.getById(dishItem.getDishId());
        User user = userService.getById(dishItem.getUserId());
        boolean paidByAll = Boolean.TRUE.equals(runtimeState.get("paidByAll"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> participants = (List<Map<String, Object>>) runtimeState.getOrDefault("participants", List.of());
        boolean paid = paidByAll || participants.stream().anyMatch(item ->
                Objects.equals(item.get("userId"), dishItem.getUserId()) && Boolean.TRUE.equals(item.get("paid"))
        );
        BigDecimal price = dish == null || dish.getPrice() == null ? BigDecimal.ZERO : dish.getPrice();
        BigDecimal lineAmount = price.multiply(BigDecimal.valueOf(dishItem.getQuantity() == null ? 0 : dishItem.getQuantity()));
        Map<String, Object> result = new HashMap<>();
        result.put("id", dishItem.getId());
        result.put("groupOrderId", dishItem.getGroupOrderId());
        result.put("dishId", dishItem.getDishId());
        result.put("quantity", dishItem.getQuantity());
        result.put("customization", dishItem.getCustomization() == null ? "" : dishItem.getCustomization());
        result.put("userId", dishItem.getUserId() == null ? "" : dishItem.getUserId());
        result.put("dishName", dish == null || dish.getName() == null ? "菜品" + dishItem.getDishId() : dish.getName());
        result.put("image", dish == null ? "" : (dish.getImage() == null ? "" : dish.getImage()));
        result.put("price", price);
        result.put("lineAmount", lineAmount);
        result.put("paid", paid);
        result.put("userName", user == null || user.getNickname() == null ? "" : user.getNickname());
        result.put("avatar", user == null ? "" : (user.getAvatar() == null ? "" : user.getAvatar()));
        return result;
    }

    private Map<String, Object> buildGroupOrderSummary(GroupOrder groupOrder) {
        return buildGroupOrderPayload(groupOrder, groupOrderService.getGroupOrderRuntimeState(groupOrder, null), null);
    }

    private Map<String, Object> buildGroupOrderPayload(GroupOrder groupOrder,
                                                       Map<String, Object> runtimeState,
                                                       String currentUserId) {
        Merchant merchant = groupOrder.getMerchantId() == null ? null : merchantService.getById(groupOrder.getMerchantId());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> participants = (List<Map<String, Object>>) runtimeState.getOrDefault("participants", List.of());

        Map<String, Object> result = new HashMap<>();
        result.put("id", groupOrder.getId());
        result.put("initiatorId", groupOrder.getInitiatorId());
        result.put("merchantId", groupOrder.getMerchantId());
        result.put("merchantName", merchant == null ? groupOrder.getMerchantId() : merchant.getName());
        result.put("merchantAvatar", merchant == null || merchant.getAvatar() == null ? "" : merchant.getAvatar());
        result.put("groupId", groupOrder.getGroupId());
        result.put("addressId", groupOrder.getAddressId());
        result.put("remark", stripConfirmedRemark(groupOrder.getRemark()));
        result.put("totalAmount", groupOrder.getTotalAmount());
        result.put("status", groupOrder.getStatus());
        result.put("confirmedTime", runtimeState.getOrDefault("confirmedTime", groupOrder.getConfirmedTime()));
        result.put("createTime", groupOrder.getCreateTime());
        result.put("updateTime", groupOrder.getUpdateTime());
        result.put("currentCount", runtimeState.getOrDefault("currentCount", participants.size()));
        result.put("maxParticipants", runtimeState.getOrDefault("maxParticipants", participants.size()));
        result.put("members", participants);
        result.put("participants", participants);
        result.put("memberCount", runtimeState.getOrDefault("currentCount", participants.size()));
        result.put("locked", runtimeState.getOrDefault("locked", groupOrder.getLocked()));
        result.put("joinable", runtimeState.getOrDefault("joinable", false));
        result.put("canConfirm", runtimeState.getOrDefault("canConfirm", false));
        result.put("canEdit", runtimeState.getOrDefault("canEdit", false));
        result.put("canLeave", runtimeState.getOrDefault("canLeave", false));
        result.put("canPay", runtimeState.getOrDefault("canPay", false));
        result.put("currentUserJoined", runtimeState.getOrDefault("currentUserJoined", false));
        result.put("currentUserPaid", runtimeState.getOrDefault("currentUserPaid", false));
        result.put("paidByAll", runtimeState.getOrDefault("paidByAll", false));
        return result;
    }

    private String stripConfirmedRemark(String remark) {
        if (remark == null || remark.isBlank()) {
            return "";
        }
        if (!remark.startsWith(GROUP_ORDER_CONFIRMED_PREFIX)) {
            return remark;
        }
        return remark.substring(GROUP_ORDER_CONFIRMED_PREFIX.length()).trim();
    }

    private String buildOrderCode(String groupOrderId) {
        String digits = groupOrderId == null ? "" : groupOrderId.replaceAll("\\D", "");
        return digits.length() > 6 ? digits.substring(digits.length() - 6) : String.format("%6s", digits).replace(' ', '0');
    }

    private BigDecimal calculatePayableAmount(String groupOrderId, String userId, String paymentType) {
        if ("all".equals(paymentType)) {
            return calculateGroupOrderTotalAmount(groupOrderId);
        }

        Map<String, Object> settlement = groupOrderService.getSettlement(groupOrderId, userId);
        return getBigDecimalValue(settlement.get("totalAmount"));
    }

    private BigDecimal calculateGroupOrderTotalAmount(String groupOrderId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<GroupOrderDish> dishes = groupOrderService.getGroupOrderDishes(groupOrderId);
        for (GroupOrderDish dishItem : dishes) {
            Dish dish = dishService.getById(dishItem.getDishId());
            if (dish == null || dish.getPrice() == null || dishItem.getQuantity() == null) {
                continue;
            }
            totalAmount = totalAmount.add(dish.getPrice().multiply(BigDecimal.valueOf(dishItem.getQuantity())));
        }
        return totalAmount;
    }

    private void handleGroupOrderWalletPayment(GroupOrder groupOrder,
                                               PaymentRecord paymentRecord,
                                               String paymentType,
                                               String paymentPassword) {
        if (paymentPassword != null && !paymentPassword.isBlank()) {
            // 当前群订单页没有输入支付密码能力，这里仅保留兼容入口。
            // 如后续前端补密码输入，可直接在此接入校验逻辑。
        }

        if (!walletService.checkBalance(paymentRecord.getUserId(), paymentRecord.getAmount())) {
            paymentRecord.setPaymentStatus("failed");
            paymentRecord.setUpdateTime(LocalDateTime.now());
            paymentService.updateById(paymentRecord);
            throw new RuntimeException("钱包余额不足");
        }

        boolean deducted = walletService.deductBalance(
                paymentRecord.getUserId(),
                paymentRecord.getAmount(),
                "支付群订单 - " + paymentRecord.getOrderId()
        );
        if (!deducted) {
            paymentRecord.setPaymentStatus("failed");
            paymentRecord.setUpdateTime(LocalDateTime.now());
            paymentService.updateById(paymentRecord);
            throw new RuntimeException("余额支付失败");
        }

        paymentRecord.setPaymentStatus("success");
        paymentRecord.setPaidTime(LocalDateTime.now());
        paymentRecord.setUpdateTime(LocalDateTime.now());
        paymentService.updateById(paymentRecord);
        Integer previousStatus = groupOrder.getStatus();
        GroupOrder updatedGroupOrder = groupOrderService.refreshGroupOrderPaymentState(
                groupOrder.getId(),
                paymentRecord.getUserId(),
                paymentRecord.getAmount(),
                paymentType
        );

        if (updatedGroupOrder != null && updatedGroupOrder.getStatus() != null && updatedGroupOrder.getStatus() == 1
                && (previousStatus == null || previousStatus != 1)) {
            NotificationUtil.createGroupOrderNotification(
                    updatedGroupOrder.getInitiatorId(),
                    NotificationTypeEnum.GROUP_ORDER_PAYMENT_SUCCESS,
                    updatedGroupOrder.getId(),
                    "待接单"
            );
            NotificationUtil.createMerchantNewOrderNotification(
                    updatedGroupOrder.getMerchantId(),
                    updatedGroupOrder.getId(),
                    "群订单"
            );
        }
    }

    private Map<String, Object> buildPaymentResponse(PaymentRecord paymentRecord,
                                                     String paymentMethod,
                                                     Map<String, Object> paymentParams) {
        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", paymentRecord.getPaymentNo());
        result.put("orderId", paymentRecord.getOrderId());
        result.put("amount", paymentRecord.getAmount());
        result.put("paymentMethod", paymentMethod);
        result.put("status", paymentRecord.getPaymentStatus());
        result.put("paymentParams", paymentParams);
        return result;
    }

    private String getStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }
}
