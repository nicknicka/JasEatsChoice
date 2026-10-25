package com.xx.jaseatschoicejava.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 包含菜品状态的菜单DTO
 * 用于返回菜品关联的菜单信息，包括菜单基本信息和菜品在该菜单中的状态
 */
@Data
public class MenuWithDishStatusDTO {
    /**
     * 菜单ID
     */
    private String id;

    /**
     * 获取菜单ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置菜单ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单类型
     */
    private String type;

    /**
     * 菜单状态
     */
    @TableField("menu_status")
    private String menu_status;

    /**
     * 菜品在菜单中的状态
     */
    @TableField("dish_status")
    private Integer dish_status;
}
