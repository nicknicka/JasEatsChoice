package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.dto.OrderDishVO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.mapper.OrderDishMapper;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单菜品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDishServiceImpl extends ServiceImpl<OrderDishMapper, OrderDish> implements OrderDishService {

    private final DishService dishService;

    @Override
    public List<OrderDishVO> getOrderDishesWithDetails(String orderId) {
        log.info("获取订单菜品详细信息，订单ID：{}", orderId);

        // 查询订单菜品列表
        LambdaQueryWrapper<OrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDish::getOrderId, orderId);
        List<OrderDish> orderDishes = this.list(queryWrapper);

        if (orderDishes == null || orderDishes.isEmpty()) {
            log.info("订单无菜品数据");
            return new ArrayList<>();
        }

        // 获取所有菜品ID
        List<String> dishIds = orderDishes.stream()
                .map(OrderDish::getDishId)
                .collect(Collectors.toList());

        // 批量查询菜品信息
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(Dish::getId, dishIds);
        List<Dish> dishes = dishService.list(dishQueryWrapper);

        // 构建菜品ID到菜品的映射
        Map<String, Dish> dishMap = dishes.stream()
                .collect(Collectors.toMap(Dish::getId, dish -> dish));

        // 组装VO对象
        List<OrderDishVO> voList = new ArrayList<>();
        for (OrderDish orderDish : orderDishes) {
            OrderDishVO vo = new OrderDishVO();
            vo.setId(orderDish.getId());
            vo.setOrderId(orderDish.getOrderId());
            vo.setDishId(orderDish.getDishId());
            vo.setQuantity(orderDish.getQuantity());
            vo.setPrice(orderDish.getPrice());
            vo.setCustomization(orderDish.getCustomization());

            // 从菜品表获取菜品信息
            Dish dish = dishMap.get(orderDish.getDishId());
            if (dish != null) {
                vo.setDish(dish); // 设置完整的菜品对象
                vo.setDishName(dish.getName());
                vo.setCategory(dish.getCategory());
                vo.setCalorie(dish.getCalorie());
            } else {
                vo.setDishName("未知菜品");
                log.warn("未找到菜品信息，菜品ID：{}", orderDish.getDishId());
            }

            voList.add(vo);
        }

        log.info("获取订单菜品详细信息成功，菜品数量：{}", voList.size());
        return voList;
    }
}
