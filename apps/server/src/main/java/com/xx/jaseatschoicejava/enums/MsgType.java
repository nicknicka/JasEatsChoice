package com.xx.jaseatschoicejava.enums;

/**
 * 消息类型枚举
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
public enum MsgType {

    /**
     * 单聊消息
     */
    SINGLE("single", "单聊消息"),

    /**
     * 群聊消息
     */
    GROUP("group", "群聊消息"),

    /**
     * 订单同步消息
     */
    ORDER_SYNC("order_sync", "订单同步消息"),

    /**
     * 订单状态消息
     */
    ORDER_STATUS("order_status", "订单状态消息"),

    /**
     * 个性化推荐消息
     */
    RECOMMENDATION("recommendation", "个性化推荐消息"),

    /**
     * 推荐更新通知
     */
    RECOMMENDATION_UPDATE("recommend_update", "推荐更新通知"),

    /**
     * 新菜品推荐
     */
    NEW_DISH_RECOMMEND("new_dish_recommend", "新菜品推荐"),

    /**
     * 实时推荐推送
     */
    REALTIME_RECOMMEND("realtime_recommend", "实时推荐推送"),

    /**
     * 文本消息
     */
    TEXT("text", "文本消息"),

    /**
     * 图片消息
     */
    IMAGE("image", "图片消息"),

    /**
     * 文件消息
     */
    FILE("file", "文件消息");

    private String value;
    private String desc;

    MsgType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}

