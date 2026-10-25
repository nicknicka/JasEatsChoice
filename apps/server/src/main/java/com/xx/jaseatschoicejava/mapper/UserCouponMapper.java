package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户优惠券Mapper接口
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 查询用户的可用优惠券（使用索引优化）
     */
    @Select("SELECT * FROM user_coupon " +
            "WHERE user_id = #{userId} " +
            "AND status = 'available' " +
            "AND expire_time > NOW() " +
            "ORDER BY expire_time ASC")
    List<UserCoupon> findAvailableCoupons(@Param("userId") String userId);

    /**
     * 查询用户的优惠券（包含已过期）
     */
    @Select("SELECT * FROM user_coupon " +
            "WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC")
    List<UserCoupon> findByUserId(@Param("userId") String userId);

    /**
     * 查询特定优惠券（用于使用和释放操作）
     */
    @Select("SELECT * FROM user_coupon " +
            "WHERE id = #{couponId} " +
            "AND user_id = #{userId} " +
            "LIMIT 1")
    UserCoupon findByIdAndUserId(@Param("couponId") String couponId, @Param("userId") String userId);

    /**
     * 统计用户可用优惠券数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon " +
            "WHERE user_id = #{userId} " +
            "AND status = 'available' " +
            "AND expire_time > NOW()")
    long countAvailableCoupons(@Param("userId") String userId);
}
