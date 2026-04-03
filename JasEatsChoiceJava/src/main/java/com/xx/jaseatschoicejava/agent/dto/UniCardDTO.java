package com.xx.jaseatschoicejava.agent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统一卡片 Schema DTO（飞书卡片架构）
 *
 * 所有卡片遵循统一的 {header, elements[], actions[]} 结构，
 * 前端根据 schema 字段识别版本，根据 element.tag 动态选择渲染组件。
 *
 * @author Claude
 * @since 2026-04-03
 */
@Data
public class UniCardDTO {

    /**
     * Schema 版本标识，固定为 "jaseat_card_v1"
     */
    private String schema = "jaseat_card_v1";

    /**
     * 卡片头部
     */
    private CardHeader header;

    /**
     * 内容元素数组（至少1个）
     */
    private List<CardElement> elements;

    /**
     * 操作按钮数组
     */
    private List<CardAction> actions;

    /**
     * 底部备注
     */
    private CardFooter footer;

    /**
     * 显示模式：inline（内嵌卡片）或 modal（全屏弹窗）
     */
    private String displayMode = "inline";

    // ========== Header ==========
    @Data
    public static class CardHeader {
        private TitleDef title;
        private String subtitle;
        private String theme;

        public CardHeader() {}

        public CardHeader(String text, String icon, String theme) {
            this.title = new TitleDef(text, icon);
            this.theme = theme;
        }
    }

    @Data
    public static class TitleDef {
        private String text;
        private String icon;

        public TitleDef() {}

        public TitleDef(String text, String icon) {
            this.text = text;
            this.icon = icon;
        }
    }

    // ========== Element 基类 ==========
    @Data
    public static class CardElement {
        private String tag;
        private Object content;

        public CardElement() {}

        public CardElement(String tag) {
            this.tag = tag;
        }
    }

    // ========== Action ==========
    @Data
    public static class CardAction {
        private String tag = "button";
        private String text;
        private String type = "default";
        private String icon;
        private Boolean disabled;
        private ActionDef action;

        public CardAction() {}

        public CardAction(String text, String type, String actionType, Object data) {
            this.text = text;
            this.type = type;
            this.action = new ActionDef(actionType, data);
        }
    }

    @Data
    public static class ActionDef {
        private String type;
        private Object data;

        public ActionDef() {}

        public ActionDef(String type, Object data) {
            this.type = type;
            this.data = data;
        }
    }

    // ========== Footer ==========
    @Data
    public static class CardFooter {
        private String note;
        private List<CardAction> actions;
    }

    // ========== Element 便捷构造 ==========

    /**
     * 菜品列表 Element
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DishListElement extends CardElement {
        private List<DishItem> dishes;

        public DishListElement() {
            super("dish_list");
        }
    }

    @Data
    public static class DishItem {
        private String dishId;
        private String dishName;
        private String imageUrl;
        private String description;
        private BigDecimal price;
        private Double rating;
        private Integer calories;
        private String category;
        private List<String> tags;
        private String merchantName;
        private String merchantId;
        private List<CardAction> actions;
    }

    /**
     * 订单列表 Element
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OrderListElement extends CardElement {
        private Integer total;
        private Integer pendingCount;
        private List<OrderItem> orders;

        public OrderListElement() {
            super("order_list");
        }
    }

    @Data
    public static class OrderItem {
        private String orderId;
        private String status;
        private String statusText;
        private String statusColor;
        private Integer dishCount;
        private BigDecimal totalAmount;
        private String createTime;
        private List<CardAction> actions;
    }

    /**
     * 营养统计 Element
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class HealthStatsElement extends CardElement {
        private List<HealthStatItem> stats;
        private String suggestion;

        public HealthStatsElement() {
            super("health_stats");
        }
    }

    @Data
    public static class HealthStatItem {
        private String label;
        private String value;
        private Integer percent;
        private String color;
    }

    /**
     * Markdown Element
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class MarkdownElement extends CardElement {
        private String content;

        public MarkdownElement() {
            super("markdown");
        }

        public MarkdownElement(String content) {
            super("markdown");
            this.content = content;
        }
    }

    /**
     * Note Element（提示/备注）
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class NoteElement extends CardElement {
        private String content;
        private String type = "info";

        public NoteElement() {
            super("note");
        }

        public NoteElement(String content, String type) {
            super("note");
            this.content = content;
            this.type = type;
        }
    }

    /**
     * 统计行 Element
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class StatsRowElement extends CardElement {
        private List<StatItem> items;

        public StatsRowElement() {
            super("stats_row");
        }
    }

    @Data
    public static class StatItem {
        private String label;
        private Object value;
        private String color;
        private String icon;
    }

    /**
     * 分割线 Element
     */
    @EqualsAndHashCode(callSuper = false)
    public static class DividerElement extends CardElement {
        public DividerElement() {
            super("divider");
        }
    }
}
