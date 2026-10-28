package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.mapper.DishFeatureMapper;
import com.xx.jaseatschoicejava.service.CollectionService;
import com.xx.jaseatschoicejava.service.ConsumeHistoryService;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatisticsServiceImplTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderDishService orderDishService;

    @Mock
    private DishService dishService;

    @Mock
    private DishFeatureMapper dishFeatureMapper;

    @Mock
    private ConsumeHistoryService consumeHistoryService;

    @Mock
    private CollectionService collectionService;

    @InjectMocks
    private UserStatisticsServiceImpl statisticsService;

    @Test
    void getCaloriesStatistics_shouldSumDishCaloriesByOrderDateAndQuantity() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        Order order = new Order();
        order.setId("order-1");
        order.setCreatedAt(today.atTime(12, 0));

        OrderDish firstDish = new OrderDish();
        firstDish.setOrderId("order-1");
        firstDish.setDishId("dish-1");
        firstDish.setQuantity(2);

        OrderDish secondDish = new OrderDish();
        secondDish.setOrderId("order-1");
        secondDish.setDishId("dish-2");
        secondDish.setQuantity(1);

        Dish chicken = new Dish();
        chicken.setId("dish-1");
        chicken.setCalorie(450);

        Dish porridge = new Dish();
        porridge.setId("dish-2");
        porridge.setCalorie(180);

        when(orderService.list(any(Wrapper.class))).thenReturn(List.of(order));
        when(orderDishService.list(any(Wrapper.class))).thenReturn(List.of(firstDish, secondDish));
        when(dishService.list(any(Wrapper.class))).thenReturn(List.of(chicken, porridge));
        when(dishFeatureMapper.selectNutritionCaloriesByDishIds(any()))
                .thenReturn(List.of(Map.of("dishId", "dish-1", "calories", new BigDecimal("500"))));

        Map<String, Object> first = statisticsService.getCaloriesStatistics("user-1", 7);
        Map<String, Object> second = statisticsService.getCaloriesStatistics("user-1", 7);

        assertEquals(new BigDecimal("1180"), first.get("totalCalories"));
        assertEquals(new BigDecimal("169"), first.get("avgCalories"));
        assertEquals(first, second);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailyCalories = (List<Map<String, Object>>) first.get("dailyCalories");
        Map<String, Object> todayCalories = dailyCalories.stream()
                .filter(item -> today.toString().equals(item.get("date")))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("1180"), todayCalories.get("calories"));
    }

    @Test
    void getCaloriesStatistics_shouldReturnZerosWhenUserHasNoCompletedOrders() {
        when(orderService.list(any(Wrapper.class))).thenReturn(List.of());

        Map<String, Object> result = statisticsService.getCaloriesStatistics("user-1", 7);

        assertEquals(BigDecimal.ZERO, result.get("totalCalories"));
        assertEquals(BigDecimal.ZERO, result.get("avgCalories"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailyCalories = (List<Map<String, Object>>) result.get("dailyCalories");
        assertEquals(7, dailyCalories.size());
        assertEquals(BigDecimal.ZERO, dailyCalories.get(6).get("calories"));
    }
}
