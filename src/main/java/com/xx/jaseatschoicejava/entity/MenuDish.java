package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class MenuDish {

    @TableId
    private Long id; // 菜单菜品关联ID

    private Long menuId; // 菜单ID

    private Long dishId; // 菜品ID

    private Integer sort; // 排序
}