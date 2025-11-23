package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;

import java.util.List;

/**
 * 群订单服务
 */
public interface GroupOrderService extends IService<GroupOrder> {

    /**
     * 创建群订单
     * @param groupOrder 群订单信息
     * @param dishItems 菜品列表
     * @return 创建是否成功
     */
    boolean createGroupOrder(GroupOrder groupOrder, List<GroupOrderDish> dishItems);

    /**
     * 根据群ID获取群订单列表
     * @param groupId 群ID
     * @return 群订单列表
     */
    List<GroupOrder> getGroupOrdersByGroupId(Long groupId);

    /**
     * 根据群订单ID获取群订单详情
     * @param groupOrderId 群订单ID
     * @return 群订单详情
     */
    GroupOrder getGroupOrderDetail(Long groupOrderId);

    /**
     * 根据群订单ID获取菜品列表
     * @param groupOrderId 群订单ID
     * @return 菜品列表
     */
    List<GroupOrderDish> getGroupOrderDishes(Long groupOrderId);
}
