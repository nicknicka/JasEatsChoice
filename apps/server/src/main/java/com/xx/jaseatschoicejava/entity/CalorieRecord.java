package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
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

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "记录ID")
    private String id; // 记录ID

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private String userId; // 用户ID

    @TableField("dish_id")
    @ApiModelProperty(value = "菜品ID")
    private String dishId; // 菜品ID

    @TableField("calorie")
    @ApiModelProperty(value = "卡路里摄入量")
    private Integer calorie; // 卡路里摄入量

    @TableField("protein")
    @ApiModelProperty(value = "蛋白质含量(g)")
    private Double protein; // 蛋白质含量(g)

    @TableField("fat")
    @ApiModelProperty(value = "脂肪含量(g)")
    private Double fat; // 脂肪含量(g)

    @TableField("carbohydrate")
    @ApiModelProperty(value = "碳水化合物含量(g)")
    private Double carbohydrate; // 碳水化合物含量(g)


    @TableField("meal_time")
    @ApiModelProperty(value = "用餐时间类型（如：早餐、午餐、晚餐、加餐）")
    private String mealTime; // 用餐时间类型（如：早餐、午餐、晚餐、加餐）

    @TableField("record_time")
    @ApiModelProperty(value = "记录时间")
    private LocalDateTime recordTime; // 记录时间

    @TableField("food_name")
    @ApiModelProperty(value = "食物名称")
    private String foodName; // 食物名称

    @TableField("description")
    @ApiModelProperty(value = "食物描述")
    private String description; // 食物描述
}
