package com.xx.jaseatschoicejava.util;

import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 订单金额计算：单价与总额只认菜品表，请求体里的价格和 totalAmount 一律丢弃。
 * <p>
 * 抽成纯函数是为了能在单测里覆盖边界（负数、越界数量、跨商家、下架菜品），
 * 也避免把这些判断散落在 controller 里。
 */
public final class OrderPricing {

    /** 单个菜品数量上限，防止构造超大数量绕过金额校验。 */
    private static final int MAX_QUANTITY = 99;

    private OrderPricing() {
    }

    /**
     * 用数据库单价重写订单菜品单价，并把重算结果写回订单总额。
     *
     * @param order      待创建的订单（会写入 totalAmount）
     * @param items      请求体带来的订单菜品（单价会被覆盖）
     * @param dishesById 菜品ID到菜品实体的映射，来自数据库
     * @return 重算后的订单总额
     * @throws IllegalArgumentException 菜品缺失、跨商家、已下架或数量非法
     */
    public static BigDecimal applyServerPrices(Order order, List<OrderDish> items, Map<String, Dish> dishesById) {
        if (order == null) {
            throw new IllegalArgumentException("订单信息不能为空");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单至少包含一个菜品");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderDish item : items) {
            if (item == null || item.getDishId() == null || item.getDishId().isBlank()) {
                throw new IllegalArgumentException("菜品ID不能为空");
            }

            Dish dish = dishesById == null ? null : dishesById.get(item.getDishId());
            if (dish == null) {
                throw new IllegalArgumentException("菜品不存在：" + item.getDishId());
            }
            if (order.getMerchantId() != null && dish.getMerchantId() != null
                    && !order.getMerchantId().equals(dish.getMerchantId())) {
                throw new IllegalArgumentException("菜品不属于该商家：" + item.getDishId());
            }
            if (Boolean.FALSE.equals(dish.getIsOnline())) {
                throw new IllegalArgumentException("菜品已下架：" + item.getDishId());
            }

            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            if (quantity <= 0 || quantity > MAX_QUANTITY) {
                throw new IllegalArgumentException("菜品数量不合法：" + item.getDishId());
            }

            BigDecimal price = dish.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("菜品价格异常：" + item.getDishId());
            }
            price = price.setScale(2, RoundingMode.HALF_UP);

            item.setQuantity(quantity);
            item.setPrice(price);
            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        total = total.setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(total);
        return total;
    }
}
