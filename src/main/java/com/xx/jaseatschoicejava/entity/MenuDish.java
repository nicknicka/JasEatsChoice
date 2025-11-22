package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 菜单菜品关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_menu_dish")
@ApiModel(description = "菜单菜品关联实体")
public class MenuDish {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "菜单菜品关联ID")
    private Long id; // 菜单菜品关联ID

    @TableField("menu_id")
    @ApiModelProperty(value = "菜单ID")
    private Long menuId; // 菜单ID

    @TableField("dish_id")
    @ApiModelProperty(value = "菜品ID")
    private Long dishId; // 菜品ID

    @TableField("sort")
    @ApiModelProperty(value = "排序")
    private Integer sort; // 排序
}