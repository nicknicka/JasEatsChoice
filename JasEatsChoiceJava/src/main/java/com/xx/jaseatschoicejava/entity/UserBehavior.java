package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户行为记录实体
 * 用于记录用户的所有行为，包括浏览、点击、下单、收藏、拒绝、分享等
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_behavior")
public class UserBehavior {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 行为类型: view(浏览)/click(点击)/order(下单)/favorite(收藏)/reject(拒绝)/share(分享)
     */
    private String behaviorType;

    /**
     * 物品类型: dish(菜品)/merchant(商家)/recipe(食谱)
     */
    private String itemType;

    /**
     * 物品ID
     */
    private String itemId;

    /**
     * 上下文信息: {time, weather, location, device}
     * 存储为JSON格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> context;

    /**
     * 行为持续时长(秒), 浏览类行为使用
     */
    private Integer duration;

    /**
     * 行为时间
     */
    private LocalDateTime createdTime;

    /**
     * 行为类型枚举
     */
    public enum BehaviorType {
        VIEW("view", "浏览"),
        CLICK("click", "点击"),
        ORDER("order", "下单"),
        FAVORITE("favorite", "收藏"),
        REJECT("reject", "拒绝"),
        SHARE("share", "分享");

        private final String code;
        private final String desc;

        BehaviorType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 物品类型枚举
     */
    public enum ItemType {
        DISH("dish", "菜品"),
        MERCHANT("merchant", "商家"),
        RECIPE("recipe", "食谱");

        private final String code;
        private final String desc;

        ItemType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
