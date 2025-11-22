package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Dish;

/**
 * 菜品Mapper接口
 */


@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
