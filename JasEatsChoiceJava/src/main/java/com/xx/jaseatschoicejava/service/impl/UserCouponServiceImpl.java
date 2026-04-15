package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.UserCoupon;
import com.xx.jaseatschoicejava.mapper.UserCouponMapper;
import com.xx.jaseatschoicejava.service.UserCouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户优惠券Service实现
 */
@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    @Override
    public List<UserCoupon> getAvailableCoupons(String userId) {
        return baseMapper.findAvailableCoupons(userId);
    }

    @Override
    public List<UserCoupon> getUserCoupons(String userId, String status) {
        String normalizedStatus = status == null ? null : status.trim().toLowerCase();
        final String statusFilter = "unused".equals(normalizedStatus) ? "available" : normalizedStatus;

        LocalDateTime now = LocalDateTime.now();

        return baseMapper.findByUserId(userId).stream()
                .peek(coupon -> coupon.setStatus(resolveCouponStatus(coupon, now)))
                .filter(coupon -> statusFilter == null || statusFilter.isEmpty() || statusFilter.equals(coupon.getStatus()))
                .toList();
    }

    private String resolveCouponStatus(UserCoupon coupon, LocalDateTime now) {
        if ("used".equals(coupon.getStatus())) {
            return "used";
        }

        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(now)) {
            return "expired";
        }

        return "available";
    }

    @Override
    public boolean checkCouponAvailable(String couponId, BigDecimal orderAmount) {
        UserCoupon coupon = baseMapper.selectById(couponId);
        if (coupon == null) {
            return false;
        }

        // 检查状态
        if (!"available".equals(coupon.getStatus())) {
            return false;
        }

        // 检查是否过期
        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 检查最低消费金额
        if (coupon.getMinAmount() != null && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useCoupon(String couponId, String orderId) {
        UserCoupon coupon = baseMapper.selectById(couponId);
        if (coupon == null || !"available".equals(coupon.getStatus())) {
            return false;
        }

        coupon.setStatus("used");
        coupon.setOrderId(orderId);
        coupon.setUseTime(LocalDateTime.now());
        return baseMapper.updateById(coupon) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseCoupon(String couponId, String orderId) {
        UserCoupon coupon = baseMapper.selectById(couponId);
        if (coupon == null || !"used".equals(coupon.getStatus())) {
            return false;
        }

        // 只能释放自己的订单
        if (!orderId.equals(coupon.getOrderId())) {
            return false;
        }

        coupon.setStatus("available");
        coupon.setOrderId(null);
        coupon.setUseTime(null);
        return baseMapper.updateById(coupon) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon issueCoupon(String userId, String name, BigDecimal amount, BigDecimal minAmount, Integer validDays) {
        UserCoupon coupon = new UserCoupon();
        coupon.setUserId(userId);
        coupon.setName(name);
        coupon.setAmount(amount);
        coupon.setMinAmount(minAmount);
        coupon.setStatus("available");

        if (validDays != null && validDays > 0) {
            coupon.setExpireTime(LocalDateTime.now().plusDays(validDays));
        }

        baseMapper.insert(coupon);
        return coupon;
    }
}
