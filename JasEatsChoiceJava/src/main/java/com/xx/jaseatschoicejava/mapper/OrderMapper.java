package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 计算已完成订单的总收入
     * @param startTime 开始时间（可为null）
     * @return 总收入
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM t_order " +
            "WHERE status = 7 " +
            "AND #{startTime} IS NULL OR create_time >= #{startTime}")
    BigDecimal sumCompletedOrdersRevenue(@Param("startTime") LocalDateTime startTime);
}
