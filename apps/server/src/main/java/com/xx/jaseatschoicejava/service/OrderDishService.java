package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.OrderDishVO;
import com.xx.jaseatschoicejava.entity.OrderDish;

import java.util.List;
import java.util.Map;

/**
 * 订单菜品服务接口
 */
public interface OrderDishService extends IService<OrderDish> {

    /**
     * 获取订单的菜品列表（包含菜品详细信息）
     * @param orderId 订单ID
     * @return 订单菜品VO列表
     */
    List<OrderDishVO> getOrderDishesWithDetails(String orderId);

    /**
     * 批量获取多个订单的菜品列表（包含菜品详细信息）
     * @param orderIds 订单ID列表
     * @return orderId -> 订单菜品VO列表
     */
    Map<String, List<OrderDishVO>> getOrderDishesWithDetailsBatch(List<String> orderIds);
}
