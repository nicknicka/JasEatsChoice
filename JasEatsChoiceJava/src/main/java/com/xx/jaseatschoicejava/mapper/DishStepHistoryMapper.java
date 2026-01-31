package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.DishStepHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜品步骤历史记录Mapper接口
 *
 * @author Claude
 * @since 2025-01-30
 */
@Mapper
public interface DishStepHistoryMapper extends BaseMapper<DishStepHistory> {

    /**
     * 根据订单ID查询所有菜品的步骤历史
     *
     * @param orderId 订单ID
     * @return 步骤历史列表
     */
    @Select("SELECT * FROM t_dish_step_history WHERE order_id = #{orderId} ORDER BY create_time ASC")
    List<DishStepHistory> listByOrderId(@Param("orderId") String orderId);

    /**
     * 根据订单菜品ID查询步骤历史
     *
     * @param orderDishId 订单菜品ID
     * @return 步骤历史列表
     */
    @Select("SELECT * FROM t_dish_step_history WHERE order_dish_id = #{orderDishId} ORDER BY create_time ASC")
    List<DishStepHistory> listByOrderDishId(@Param("orderDishId") String orderDishId);

    /**
     * 查询订单中指定步骤的所有菜品
     *
     * @param orderId 订单ID
     * @param stepStatus 步骤状态
     * @return 步骤历史列表
     */
    @Select("SELECT * FROM t_dish_step_history WHERE order_id = #{orderId} AND new_step_status = #{stepStatus} ORDER BY create_time DESC")
    List<DishStepHistory> listByOrderIdAndStepStatus(@Param("orderId") String orderId, @Param("stepStatus") Integer stepStatus);
}
