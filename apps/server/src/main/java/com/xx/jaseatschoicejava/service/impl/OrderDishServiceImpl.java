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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        Map<String, List<OrderDishVO>> batch = getOrderDishesWithDetailsBatch(Collections.singletonList(orderId));
        List<OrderDishVO> voList = batch.getOrDefault(orderId, new ArrayList<>());

        log.info("获取订单菜品详细信息成功，菜品数量：{}", voList.size());
        return voList;
    }

    @Override
    public Map<String, List<OrderDishVO>> getOrderDishesWithDetailsBatch(List<String> orderIds) {
        // 空入参直接返回空 Map，避免拼出非法的 IN ()
        if (orderIds == null || orderIds.isEmpty()) {
            return new HashMap<>();
        }

        // 第 1 次查询：一次取出所有订单的 order_dish
        LambdaQueryWrapper<OrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderDish::getOrderId, orderIds);
        List<OrderDish> orderDishes = this.list(queryWrapper);

        Map<String, List<OrderDishVO>> result = new HashMap<>();
        if (orderDishes == null || orderDishes.isEmpty()) {
            return result;
        }

        // 第 2 次查询：去重后一次取出所有涉及的菜品
        List<String> dishIds = orderDishes.stream()
                .map(OrderDish::getDishId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Dish> dishMap = new HashMap<>();
        if (!dishIds.isEmpty()) {
            LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
            dishQueryWrapper.in(Dish::getId, dishIds);
            List<Dish> dishes = dishService.list(dishQueryWrapper);
            dishMap = dishes.stream()
                    .collect(Collectors.toMap(Dish::getId, dish -> dish, (existing, ignored) -> existing));
        }

        // 组装VO对象，按 orderId 分组
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

            result.computeIfAbsent(orderDish.getOrderId(), key -> new ArrayList<>()).add(vo);
        }

        return result;
    }
}
