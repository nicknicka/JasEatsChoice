package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 卡路里摄入记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_calorie_record")
@ApiModel(description = "卡路里摄入记录实体")
public class CalorieRecord {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "记录ID")
    private Long id; // 记录ID

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private Long userId; // 用户ID

    @TableField("dish_id")
    @ApiModelProperty(value = "菜品ID")
    private Long dishId; // 菜品ID

    @TableField("calorie")
    @ApiModelProperty(value = "卡路里摄入量")
    private Integer calorie; // 卡路里摄入量

    @TableField("meal_time")
    @ApiModelProperty(value = "用餐时间类型（如：早餐、午餐、晚餐、加餐）")
    private String mealTime; // 用餐时间类型（如：早餐、午餐、晚餐、加餐）

    @TableField("record_time")
    @ApiModelProperty(value = "记录时间")
    private LocalDateTime recordTime; // 记录时间
}
