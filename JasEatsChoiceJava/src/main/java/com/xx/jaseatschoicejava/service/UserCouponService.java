package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.UserCoupon;

import java.math.BigDecimal;

/**
 * 用户优惠券Service接口
 */
public interface UserCouponService extends IService<UserCoupon> {

    /**
     * 获取用户的可用优惠券列表
     */
    java.util.List<UserCoupon> getAvailableCoupons(String userId);

    /**
     * 获取用户优惠券列表
     * @param userId 用户ID
     * @param status 状态筛选：available/unused/used/expired/null
     */
    java.util.List<UserCoupon> getUserCoupons(String userId, String status);

    /**
     * 检查优惠券是否可用
     */
    boolean checkCouponAvailable(String couponId, BigDecimal orderAmount);

    /**
     * 使用优惠券
     */
    boolean useCoupon(String couponId, String orderId);

    /**
     * 释放优惠券（取消订单时）
     */
    boolean releaseCoupon(String couponId, String orderId);

    /**
     * 发放优惠券给用户
     */
    UserCoupon issueCoupon(String userId, String name, BigDecimal amount, BigDecimal minAmount, Integer validDays);
}
