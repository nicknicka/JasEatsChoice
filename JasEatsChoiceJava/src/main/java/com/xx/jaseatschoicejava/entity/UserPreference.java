package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户推荐偏好
 */
@Data
@TableName("t_user_preference")
public class UserPreference {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 标签权重，JSON格式（如 {"spicy": 0.1, "sweet": 0.8}）
     */
    private String tagWeights;

    /**
     * 是否关闭天气推荐
     */
    private Boolean disableWeatherRecommend;

    /**
     * 饮食目标
     */
    private String dietGoal;

    /**
     * 过敏食材列表，JSON格式
     */
    private String allergies;

    /**
     * 是否允许AI使用个人数据（true-允许，false-不允许）
     */
    private Boolean enableAiPersonalData;

    /**
     * 更新时间
     */
    private String updateTime;
}
