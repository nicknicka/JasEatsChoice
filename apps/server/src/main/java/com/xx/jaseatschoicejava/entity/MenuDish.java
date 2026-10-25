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

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    @ApiModelProperty(value = "菜单菜品关联ID")
    private String id; // 菜单菜品关联ID

    @TableField("menu_id")
    @ApiModelProperty(value = "菜单ID")
    private String menuId; // 菜单ID

    @TableField("dish_id")
    @ApiModelProperty(value = "菜品ID")
    private String dishId; // 菜品ID

    @TableField("sort")
    @ApiModelProperty(value = "排序")
    private Integer sort; // 排序

    @TableField("status")
    @ApiModelProperty(value = "菜品在菜单中的状态：1-上架，0-下架")
    private Integer status = 1; // 菜品在菜单中的状态，默认为上架
}