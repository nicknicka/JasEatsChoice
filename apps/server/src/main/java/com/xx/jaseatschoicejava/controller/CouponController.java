package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.UserCoupon;
import com.xx.jaseatschoicejava.service.UserCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户优惠券Controller
 */
@Api(tags = "用户优惠券管理")
@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final UserCouponService userCouponService;

    @ApiOperation("获取用户优惠券列表")
    @GetMapping("/user")
    public ResponseResult<List<UserCoupon>> getUserCoupons(
            @ApiParam("用户ID") @RequestParam String userId,
            @ApiParam("优惠券状态：available/unused/used/expired") @RequestParam(required = false) String status) {
        List<UserCoupon> coupons = userCouponService.getUserCoupons(userId, status);
        return ResponseResult.success(coupons);
    }

    @ApiOperation("检查优惠券是否可用")
    @PostMapping("/check")
    public ResponseResult<Map<String, Object>> checkCouponAvailable(
            @ApiParam("优惠券ID") @RequestParam String couponId,
            @ApiParam("订单金额") @RequestParam BigDecimal orderAmount) {

        boolean available = userCouponService.checkCouponAvailable(couponId, orderAmount);

        Map<String, Object> result = new HashMap<>();
        result.put("available", available);
        result.put("note", available ? "优惠券可用" : "优惠券不可用或已过期");

        return ResponseResult.success(result);
    }

    @ApiOperation("使用优惠券")
    @PostMapping("/use")
    public ResponseResult<Map<String, String>> useCoupon(
            @ApiParam("优惠券ID") @RequestParam String couponId,
            @ApiParam("订单ID") @RequestParam String orderId) {

        boolean success = userCouponService.useCoupon(couponId, orderId);

        Map<String, String> result = new HashMap<>();
        result.put("couponId", couponId);
        result.put("orderId", orderId);

        if (success) {
            return ResponseResult.success(result, "优惠券使用成功");
        } else {
            return ResponseResult.fail("400", "优惠券使用失败");
        }
    }

    @ApiOperation("释放优惠券")
    @PostMapping("/release")
    public ResponseResult<Map<String, String>> releaseCoupon(
            @ApiParam("优惠券ID") @RequestParam String couponId,
            @ApiParam("订单ID") @RequestParam String orderId) {

        boolean success = userCouponService.releaseCoupon(couponId, orderId);

        Map<String, String> result = new HashMap<>();
        result.put("couponId", couponId);
        result.put("orderId", orderId);

        if (success) {
            return ResponseResult.success(result, "优惠券已释放");
        } else {
            return ResponseResult.fail("400", "优惠券释放失败");
        }
    }

    @ApiOperation("发放测试优惠券（仅开发环境）")
    @PostMapping("/issue-test")
    public ResponseResult<UserCoupon> issueTestCoupon(
            @ApiParam("用户ID") @RequestParam String userId) {
        // 发放一个测试优惠券
        UserCoupon coupon = userCouponService.issueCoupon(
                userId,
                "新用户专享50元优惠券",
                new BigDecimal("50.00"),
                new BigDecimal("100.00"),
                30
        );
        return ResponseResult.success(coupon);
    }
}
