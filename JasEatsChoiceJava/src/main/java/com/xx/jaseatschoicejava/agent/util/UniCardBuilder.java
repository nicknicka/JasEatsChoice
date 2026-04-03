package com.xx.jaseatschoicejava.agent.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.dto.UniCardDTO;
import com.xx.jaseatschoicejava.agent.dto.UniCardDTO.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * UniCard 流式构建工具
 *
 * 提供简洁的 Builder API，方便各工具类快速构建统一卡片格式。
 *
 * <pre>
 * // 菌品推荐卡片
 * String json = UniCardBuilder.create("dish")
 *     .title("菜品推荐", "🍽️")
 *     .subtitle("为您找到3道菜品")
 *     .addDishList(dishes)
 *     .addAction("加入购物车", "primary", "add_to_cart", dishData)
 *     .buildJson();
 * </pre>
 *
 * @author Claude
 * @since 2026-04-03
 */
public class UniCardBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UniCardDTO card = new UniCardDTO();
    private final List<UniCardDTO.CardElement> elements = new ArrayList<>();
    private final List<UniCardDTO.CardAction> actions = new ArrayList<>();

    private UniCardBuilder(String theme) {
        card.setHeader(new UniCardDTO.CardHeader(null, null, theme));
    }

    // ========== 创建 ==========

    public static UniCardBuilder create(String theme) {
        return new UniCardBuilder(theme);
    }

    // ========== Header ==========

    public UniCardBuilder title(String text, String icon) {
        card.setHeader(new UniCardDTO.CardHeader(text, icon, card.getHeader().getTheme()));
        return this;
    }

    public UniCardBuilder subtitle(String subtitle) {
        card.getHeader().setSubtitle(subtitle);
        return this;
    }

    // ========== Elements ==========

    /**
     * 添加菜品列表 Element
     */
    public UniCardBuilder addDishList(List<UniCardDTO.DishItem> dishes) {
        UniCardDTO.DishListElement el = new UniCardDTO.DishListElement();
        el.setDishes(dishes);
        elements.add(el);
        return this;
    }

    /**
     * 添加订单列表 Element
     */
    public UniCardBuilder addOrderList(List<UniCardDTO.OrderItem> orders, Integer total, Integer pendingCount) {
        UniCardDTO.OrderListElement el = new UniCardDTO.OrderListElement();
        el.setOrders(orders);
        el.setTotal(total);
        el.setPendingCount(pendingCount);
        elements.add(el);
        return this;
    }

    /**
     * 添加营养统计 Element
     */
    public UniCardBuilder addHealthStats(List<UniCardDTO.HealthStatItem> stats, String suggestion) {
        UniCardDTO.HealthStatsElement el = new UniCardDTO.HealthStatsElement();
        el.setStats(stats);
        el.setSuggestion(suggestion);
        elements.add(el);
        return this;
    }

    /**
     * 添加 Markdown Element
     */
    public UniCardBuilder addMarkdown(String content) {
        elements.add(new UniCardDTO.MarkdownElement(content));
        return this;
    }

    /**
     * 添加备注 Element
     */
    public UniCardBuilder addNote(String content, String type) {
        elements.add(new UniCardDTO.NoteElement(content, type != null ? type : "info"));
        return this;
    }

    /**
     * 添加统计行 Element
     */
    public UniCardBuilder addStatsRow(List<UniCardDTO.StatItem> items) {
        UniCardDTO.StatsRowElement el = new UniCardDTO.StatsRowElement();
        el.setItems(items);
        elements.add(el);
        return this;
    }

    /**
     * 添加分割线 Element
     */
    public UniCardBuilder addDivider() {
        elements.add(new UniCardDTO.DividerElement());
        return this;
    }

    /**
     * 添加通用 Element（用于扩展）
     */
    public UniCardBuilder addElement(UniCardDTO.CardElement element) {
        elements.add(element);
        return this;
    }

    // ========== Actions ==========

    /**
     * 添加操作按钮
     */
    public UniCardBuilder addAction(String text, String type, String actionType, Object data) {
        actions.add(new UniCardDTO.CardAction(text, type, actionType, data));
        return this;
    }

    // ========== Footer ==========

    public UniCardBuilder footerNote(String note) {
        if (card.getFooter() == null) {
            card.setFooter(new UniCardDTO.CardFooter());
        }
        card.getFooter().setNote(note);
        return this;
    }

    // ========== Display Mode ==========

    public UniCardBuilder displayMode(String mode) {
        card.setDisplayMode(mode);
        return this;
    }

    // ========== 构建结果 ==========

    /**
     * 构建为 DTO 对象
     */
    public UniCardDTO build() {
        card.setElements(elements);
        if (!actions.isEmpty()) {
            card.setActions(actions);
        }
        return card;
    }

    /**
     * 构建为 JSON 字符串（带 [CARD_DATA_START] / [CARD_DATA_END] 标记）
     */
    public String buildJson() {
        try {
            return "[CARD_DATA_START]\n" + MAPPER.writeValueAsString(build()) + "\n[CARD_DATA_END]";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("UniCard JSON序列化失败", e);
        }
    }

    // ========== 便捷静态工厂方法 ==========

    /**
     * 从菜品推荐数据快速构建菜品卡片
     */
    public static UniCardDTO.DishItem createDishItem(String dishId, String dishName, BigDecimal price,
                                                    Double rating, String imageUrl, String description,
                                                    String category, List<String> tags) {
        UniCardDTO.DishItem item = new UniCardDTO.DishItem();
        item.setDishId(dishId);
        item.setDishName(dishName);
        item.setPrice(price);
        item.setRating(rating);
        item.setImageUrl(imageUrl);
        item.setDescription(description);
        item.setCategory(category);
        item.setTags(tags != null ? tags : new ArrayList<>());
        return item;
    }

    /**
     * 从营养数据快速构建统计项
     */
    public static UniCardDTO.HealthStatItem createHealthStat(String label, String value, int percent, String color) {
        UniCardDTO.HealthStatItem item = new UniCardDTO.HealthStatItem();
        item.setLabel(label);
        item.setValue(value);
        item.setPercent(percent);
        item.setColor(color);
        return item;
    }
}
