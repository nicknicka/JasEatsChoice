package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_dish")
@ApiModel(description = "菜品实体")
public class Dish {

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    @ApiModelProperty(value = "菜品ID")
    @JsonProperty("dishId")  // 序列化时使用dishId
    private String id; // 菜品ID

    @TableField("merchant_id")
    @ApiModelProperty(value = "商家ID")
    private String merchantId; // 商家ID

    @TableField(exist = false)  // 不映射到数据库字段
    @ApiModelProperty(value = "商家名称")
    private String merchantName; // 商家名称（用于前端显示）

    @TableField("name")
    @ApiModelProperty(value = "菜品名称")
    @JsonProperty("dishName")  // 序列化时使用dishName
    private String name; // 菜品名称

    @TableField("category")
    @ApiModelProperty(value = "菜品分类")
    private String category; // 菜品分类

    @TableField("price")
    @ApiModelProperty(value = "菜品价格")
    private BigDecimal price; // 菜品价格

    @TableField("calorie")
    @ApiModelProperty(value = "卡路里含量")
    private Integer calorie; // 卡路里含量

    @TableField("estimated_cooking_minutes")
    @ApiModelProperty(value = "预估烹饪时长（分钟）")
    private Integer estimatedCookingMinutes; // 预估烹饪时长（分钟）

    @TableField("step_template")
    @ApiModelProperty(value = "烹饪流程模板：NORMAL-正餐流程, FAST-快餐流程, CUSTOM-自定义流程")
    private String stepTemplate; // 烹饪流程模板

    @TableField(exist = false)  // 虚拟字段，根据stepTemplate计算
    @ApiModelProperty(value = "是否为快餐（根据stepTemplate计算）")
    private Boolean isFastFood; // 是否为快餐（虚拟字段）

    @TableField("ingredients")
    @ApiModelProperty(value = "食材列表（JSON格式）")
    private String ingredients; // 食材列表（JSON格式）

    @TableField("description")
    @ApiModelProperty(value = "菜品描述")
    private String description; // 菜品描述

    @TableField("image")
    @ApiModelProperty(value = "菜品图片URL")
    private String image; // 菜品图片URL

    @TableField("score")
    @ApiModelProperty(value = "推荐得分（用于推荐算法计算）")
    private BigDecimal score; // 推荐得分

    @TableField("avg_rating")
    @ApiModelProperty(value = "平均评分（0-5分）")
    private BigDecimal avgRating; // 平均评分

    @TableField("is_online")
    @ApiModelProperty(value = "是否上架：true-上架，false-下架")
    private Boolean isOnline; // 是否上架

    // ========== 统计字段 ==========

    @TableField("view_count")
    @ApiModelProperty(value = "浏览次数")
    private Integer viewCount; // 浏览次数

    @TableField("order_count")
    @ApiModelProperty(value = "订单次数")
    private Integer orderCount; // 订单次数

    @TableField("favorite_count")
    @ApiModelProperty(value = "收藏次数")
    private Integer favoriteCount; // 收藏次数

    @TableField("tags")
    @ApiModelProperty(value = "标签列表（JSON格式）")
    private JsonNode tags; // 标签列表（JSON格式）

    @TableField(exist = false)  // 不映射到数据库字段
    @ApiModelProperty(value = "状态代码（用于前端显示）")
    private String statusCode; // 状态代码：ACTIVE-上架，INACTIVE-下架（根据isOnline计算）

    @TableField("audit_status")
    @ApiModelProperty(value = "审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝")
    private String auditStatus; // 审核状态

    @TableField("audit_comment")
    @ApiModelProperty(value = "审核意见")
    private String auditComment; // 审核意见

    @TableField("audit_time")
    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime; // 审核时间

    @TableField("audit_admin_id")
    @ApiModelProperty(value = "审核管理员ID")
    private Long auditAdminId; // 审核管理员ID

    @TableField(exist = false)  // 不映射到数据库字段
    @ApiModelProperty(value = "提交时间（用于前端显示）")
    private String submitTime; // 提交时间（用于前端显示，映射自create_time）

    @TableField("stock")
    @ApiModelProperty(value = "库存数量")
    private Integer stock; // 库存数量

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间

    // ========== 兼容方法（向后兼容旧代码）==========

    /**
     * 兼容旧代码：获取上架状态
     * @deprecated 使用 getIsOnline() 替代
     */
    @Deprecated
    public Boolean getStatus() {
        return isOnline;
    }

    /**
     * 兼容旧代码：设置上架状态
     * @deprecated 使用 setIsOnline(Boolean) 替代
     */
    @Deprecated
    public void setStatus(Boolean status) {
        this.isOnline = status;
    }

    /**
     * 获取是否为快餐（根据stepTemplate计算）
     */
    public Boolean getIsFastFood() {
        return "FAST".equalsIgnoreCase(stepTemplate);
    }

    /**
     * 兼容旧代码：获取预估烹饪时长
     * @deprecated 使用 getEstimatedCookingMinutes() 替代
     */
    @Deprecated
    public Integer getCookingMinutes() {
        return estimatedCookingMinutes;
    }

    /**
     * 兼容旧代码：设置预估烹饪时长
     * @deprecated 使用 setEstimatedCookingMinutes(Integer) 替代
     */
    @Deprecated
    public void setCookingMinutes(Integer minutes) {
        this.estimatedCookingMinutes = minutes;
    }
}