package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Order;

/**
 * 订单Mapper接口
 */


@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
