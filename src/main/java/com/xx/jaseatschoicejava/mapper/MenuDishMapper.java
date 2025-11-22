package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.MenuDish;

/**
 * 菜单菜品关联Mapper接口
 */


@Mapper
public interface MenuDishMapper extends BaseMapper<MenuDish> {
}
