package com.xx.jaseatschoicejava.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 卡片类型注解
 * 标记工具方法应该生成哪种类型的卡片数据
 *
 * 使用示例：
 * <pre>
 * {@code
 * @Tool("查询用户订单列表")
 * @CardType("order_list_card")
 * public String listOrders(String userId) {
 *     // ...
 * }
 * }
 * </pre>
 *
 * @author Claude
 * @since 2026-03-24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CardType {

    /**
     * 卡片类型
     * 必须与前端卡片组件的 messageType 匹配
     *
     * 常用卡片类型：
     * - "order_list_card" - 订单列表卡片
     * - "user_info_card" - 用户信息卡片
     * - "dish_recommendation_card" - 菜品推荐卡片
     * - "nutrition_analysis_card" - 营养分析卡片
     *
     * @return 卡片类型标识
     */
    String value();

    /**
     * 是否优先生成卡片
     * 如果一个请求中调用了多个工具，只有 priority 最高的会生成卡片
     *
     * @return 优先级，默认为0
     */
    int priority() default 0;
}
