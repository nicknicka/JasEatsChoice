package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.entity.UserCoupon;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.UserCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@Api(tags = "支付管理")
@RestController
@RequestMapping("/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserCouponService userCouponService;

    /**
     * 获取订单支付信息
     * PAY-001: 调用API获取订单详情
     */
    @ApiOperation("获取订单支付信息")
    @GetMapping("/order/{orderId}")
    public ResponseResult<Map<String, Object>> getOrderPaymentInfo(@PathVariable String orderId) {
        try {
            Order order = orderService.getById(orderId);
            if (order == null) {
                return ResponseResult.fail("404", "订单不存在");
            }

            // 构建支付信息
            Map<String, Object> paymentInfo = new HashMap<>();
            paymentInfo.put("orderId", order.getId());
            paymentInfo.put("orderNo", order.getId()); // 使用订单ID作为订单号
            paymentInfo.put("totalAmount", order.getTotalAmount());
            paymentInfo.put("status", order.getStatus());

            // 已支付金额
            BigDecimal paidAmount = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
            paymentInfo.put("paidAmount", paidAmount);

            // 获取商家信息
            paymentInfo.put("merchantId", order.getMerchantId());
            paymentInfo.put("merchantName", "商家名称"); // 可以从MerchantService获取

            // 计算优惠金额
            BigDecimal discount = BigDecimal.ZERO;
            if (order.getPaidAmount() != null && order.getTotalAmount() != null) {
                discount = order.getTotalAmount().subtract(order.getPaidAmount());
            }
            paymentInfo.put("discount", discount);
            paymentInfo.put("finalAmount", order.getTotalAmount().subtract(discount));

            return ResponseResult.success(paymentInfo);
        } catch (Exception e) {
            log.error("获取订单支付信息失败，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "获取支付信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用优惠券
     * PAY-002: 调用API获取可用优惠券
     */
    @ApiOperation("获取可用优惠券")
    @GetMapping("/coupons")
    public ResponseResult<List<UserCoupon>> getAvailableCoupons(
            @ApiParam("用户ID") @RequestParam String userId,
            @ApiParam("订单金额") @RequestParam(required = false) BigDecimal orderAmount) {
        try {
            List<UserCoupon> coupons = userCouponService.getAvailableCoupons(userId);

            // 如果提供了订单金额，过滤出可用的优惠券
            if (orderAmount != null && !coupons.isEmpty()) {
                coupons = coupons.stream()
                        .filter(coupon -> userCouponService.checkCouponAvailable(coupon.getId(), orderAmount))
                        .toList();
            }

            return ResponseResult.success(coupons);
        } catch (Exception e) {
            log.error("获取可用优惠券失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取优惠券失败：" + e.getMessage());
        }
    }

    /**
     * 创建支付订单
     * PAY-003: 调用统一支付API
     */
    @ApiOperation("创建支付订单")
    @PostMapping("/create")
    public ResponseResult<Map<String, Object>> createPayment(@RequestBody Map<String, Object> request) {
        try {
            String orderId = (String) request.get("orderId");
            String userId = (String) request.get("userId");
            String paymentMethod = (String) request.get("paymentMethod");
            String couponId = (String) request.get("couponId");

            // 获取订单信息
            Order order = orderService.getById(orderId);
            if (order == null) {
                return ResponseResult.fail("404", "订单不存在");
            }

            // 检查订单状态
            if (order.getStatus() != 0) {
                return ResponseResult.fail("400", "订单状态异常，无法支付");
            }

            // 计算支付金额
            BigDecimal finalAmount = order.getTotalAmount();

            // 应用优惠券
            if (couponId != null && !couponId.isEmpty()) {
                boolean couponAvailable = userCouponService.checkCouponAvailable(couponId, finalAmount);
                if (couponAvailable) {
                    UserCoupon coupon = userCouponService.getById(couponId);
                    if (coupon != null && coupon.getAmount() != null) {
                        finalAmount = finalAmount.subtract(coupon.getAmount());
                        // 使用优惠券
                        userCouponService.useCoupon(couponId, orderId);
                    }
                }
            }

            // 确保金额不为负数
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            // 创建支付记录
            PaymentRecord paymentRecord = paymentService.createPayment(
                    orderId,
                    userId,
                    order.getMerchantId(),
                    finalAmount,
                    paymentMethod
            );

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", paymentRecord.getPaymentNo());
            result.put("orderId", orderId);
            result.put("amount", finalAmount);
            result.put("paymentMethod", paymentMethod);
            result.put("status", paymentRecord.getPaymentStatus());

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            return ResponseResult.fail("500", "创建支付订单失败：" + e.getMessage());
        }
    }

    /**
     * 微信支付
     * PAY-004: 调用微信支付
     */
    @ApiOperation("微信支付")
    @PostMapping("/wechat")
    public ResponseResult<Map<String, Object>> wechatPay(@RequestBody Map<String, Object> request) {
        try {
            String paymentNo = (String) request.get("paymentNo");

            // TODO: 集成微信支付SDK
            // 这里需要调用微信支付API，生成支付参数
            // 返回支付参数给前端，前端调起微信支付

            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", paymentNo);
            result.put("status", "pending");
            result.put("message", "微信支付功能开发中");

            log.warn("微信支付功能尚未实现，支付流水号：{}", paymentNo);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("微信支付失败", e);
            return ResponseResult.fail("500", "微信支付失败：" + e.getMessage());
        }
    }

    /**
     * 支付宝支付
     * PAY-005: 调用支付宝支付
     */
    @ApiOperation("支付宝支付")
    @PostMapping("/alipay")
    public ResponseResult<Map<String, Object>> alipay(@RequestBody Map<String, Object> request) {
        try {
            String paymentNo = (String) request.get("paymentNo");

            // TODO: 集成支付宝支付SDK
            // 这里需要调用支付宝支付API，生成支付参数
            // 返回支付参数给前端，前端调起支付宝支付

            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", paymentNo);
            result.put("status", "pending");
            result.put("message", "支付宝支付功能开发中");

            log.warn("支付宝支付功能尚未实现，支付流水号：{}", paymentNo);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("支付宝支付失败", e);
            return ResponseResult.fail("500", "支付宝支付失败：" + e.getMessage());
        }
    }

    /**
     * 余额支付
     * PAY-006: 调用余额支付API
     */
    @ApiOperation("余额支付")
    @PostMapping("/balance")
    public ResponseResult<Map<String, Object>> balancePay(@RequestBody Map<String, Object> request) {
        try {
            String paymentNo = (String) request.get("paymentNo");
            String paymentPassword = (String) request.get("paymentPassword");

            PaymentRecord paymentRecord = paymentService.getPaymentByPaymentNo(paymentNo);
            if (paymentRecord == null) {
                return ResponseResult.fail("404", "支付记录不存在");
            }

            // 检查支付状态
            if (!"pending".equals(paymentRecord.getPaymentStatus())) {
                return ResponseResult.fail("400", "支付状态异常");
            }

            // 如果提供了支付密码，使用带密码验证的支付方式
            boolean success;
            if (paymentPassword != null && !paymentPassword.isEmpty()) {
                success = paymentService.walletPaymentWithPassword(paymentNo, paymentPassword);
            } else {
                success = paymentService.walletPayment(paymentNo);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", paymentNo);
            result.put("status", success ? "success" : "failed");

            if (success) {
                return ResponseResult.success(result, "支付成功");
            } else {
                return ResponseResult.fail("500", "支付失败");
            }
        } catch (Exception e) {
            log.error("余额支付失败", e);
            return ResponseResult.fail("500", "余额支付失败：" + e.getMessage());
        }
    }

    /**
     * 查询支付状态
     * PAY-007: 轮询查询支付状态
     */
    @ApiOperation("查询支付状态")
    @GetMapping("/status/{paymentNo}")
    public ResponseResult<Map<String, Object>> getPaymentStatus(@PathVariable String paymentNo) {
        try {
            PaymentRecord paymentRecord = paymentService.getPaymentByPaymentNo(paymentNo);
            if (paymentRecord == null) {
                return ResponseResult.fail("404", "支付记录不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", paymentNo);
            result.put("status", paymentRecord.getPaymentStatus());
            result.put("amount", paymentRecord.getAmount());
            result.put("paymentMethod", paymentRecord.getPaymentMethod());

            // 如果支付成功，返回订单信息
            if ("success".equals(paymentRecord.getPaymentStatus())) {
                Order order = orderService.getById(paymentRecord.getOrderId());
                if (order != null) {
                    result.put("orderStatus", order.getStatus());
                }
            }

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("查询支付状态失败，支付流水号：{}", paymentNo, e);
            return ResponseResult.fail("500", "查询支付状态失败：" + e.getMessage());
        }
    }

    /**
     * 查询订单支付状态（通过订单ID）
     */
    @ApiOperation("查询订单支付状态")
    @GetMapping("/order/{orderId}/status")
    public ResponseResult<Map<String, Object>> getOrderByPaymentStatus(@PathVariable String orderId) {
        try {
            PaymentRecord paymentRecord = paymentService.getPaymentByOrderId(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);

            if (paymentRecord != null) {
                result.put("paymentNo", paymentRecord.getPaymentNo());
                result.put("status", paymentRecord.getPaymentStatus());
                result.put("amount", paymentRecord.getAmount());
                result.put("paymentMethod", paymentRecord.getPaymentMethod());
            } else {
                result.put("status", "unpaid");
            }

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("查询订单支付状态失败，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "查询支付状态失败：" + e.getMessage());
        }
    }
}
