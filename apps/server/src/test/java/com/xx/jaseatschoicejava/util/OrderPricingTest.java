package com.xx.jaseatschoicejava.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 订单金额只认菜品表：请求体里的单价与总额都必须被覆盖。
 */
class OrderPricingTest {

    private static final String MERCHANT_ID = "M1";

    @Test
    void recalculatesTotalFromDatabasePriceAndIgnoresClientAmount() {
        Order order = orderWithClientAmount("0.01");
        OrderDish item = item("D1", 2, new BigDecimal("0.01"));
        Dish dish = dish("D1", "12.50", true);

        BigDecimal total = OrderPricing.applyServerPrices(order, List.of(item), Map.of("D1", dish));

        assertEquals(new BigDecimal("25.00"), total);
        assertEquals(new BigDecimal("25.00"), order.getTotalAmount());
        // 客户端伪造的 0.01 单价被数据库单价覆盖
        assertEquals(new BigDecimal("12.50"), item.getPrice());
    }

    @Test
    void sumsMultipleDishes() {
        Order order = orderWithClientAmount("0.01");
        OrderDish first = item("D1", 1, new BigDecimal("0.01"));
        OrderDish second = item("D2", 3, new BigDecimal("0.01"));

        OrderPricing.applyServerPrices(order, List.of(first, second), Map.of(
                "D1", dish("D1", "8.80", true),
                "D2", dish("D2", "3.10", true)));

        assertEquals(new BigDecimal("18.10"), order.getTotalAmount());
    }

    @Test
    void rejectsEmptyDishList() {
        Order order = orderWithClientAmount("0.01");
        assertThrows(IllegalArgumentException.class,
                () -> OrderPricing.applyServerPrices(order, List.of(), Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> OrderPricing.applyServerPrices(order, null, Map.of()));
    }

    @Test
    void rejectsUnknownDish() {
        Order order = orderWithClientAmount("0.01");
        OrderDish item = item("D404", 1, new BigDecimal("10.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OrderPricing.applyServerPrices(order, List.of(item), Map.of()));
        assertEquals("菜品不存在：D404", exception.getMessage());
    }

    @Test
    void rejectsDishFromAnotherMerchant() {
        Order order = orderWithClientAmount("0.01");
        OrderDish item = item("D1", 1, new BigDecimal("10.00"));
        Dish dish = dish("D1", "10.00", true);
        dish.setMerchantId("M2");

        assertThrows(IllegalArgumentException.class,
                () -> OrderPricing.applyServerPrices(order, List.of(item), Map.of("D1", dish)));
    }

    @Test
    void rejectsOfflineDish() {
        Order order = orderWithClientAmount("0.01");
        OrderDish item = item("D1", 1, new BigDecimal("10.00"));

        assertThrows(IllegalArgumentException.class,
                () -> OrderPricing.applyServerPrices(order, List.of(item), Map.of("D1", dish("D1", "10.00", false))));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        Order order = orderWithClientAmount("0.01");
        OrderDish item = item("D1", 0, new BigDecimal("10.00"));

        assertThrows(IllegalArgumentException.class,
                () -> OrderPricing.applyServerPrices(order, List.of(item), Map.of("D1", dish("D1", "10.00", true))));
    }

    @Test
    void rejectsQuantityAboveUpperBound() {
        Order order = orderWithClientAmount("0.01");
        OrderDish item = item("D1", 1000, new BigDecimal("10.00"));

        assertThrows(IllegalArgumentException.class,
                () -> OrderPricing.applyServerPrices(order, List.of(item), Map.of("D1", dish("D1", "10.00", true))));
    }

    private Order orderWithClientAmount(String totalAmount) {
        Order order = new Order();
        order.setMerchantId(MERCHANT_ID);
        order.setTotalAmount(new BigDecimal(totalAmount));
        return order;
    }

    private OrderDish item(String dishId, int quantity, BigDecimal price) {
        OrderDish item = new OrderDish();
        item.setDishId(dishId);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    private Dish dish(String dishId, String price, boolean online) {
        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setMerchantId(MERCHANT_ID);
        dish.setPrice(new BigDecimal(price));
        dish.setIsOnline(online);
        return dish;
    }
}
