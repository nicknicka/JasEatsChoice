package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.OrderDish;

/**
 * 订单菜品Mapper接口
 */


@Mapper
public interface OrderDishMapper extends BaseMapper<OrderDish> {
}
