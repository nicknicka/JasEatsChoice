package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.mapper.OrderDishMapper;
import com.xx.jaseatschoicejava.service.OrderDishService;
import org.springframework.stereotype.Service;

/**
 * 订单菜品服务实现
 */
@Service
public class OrderDishServiceImpl extends ServiceImpl<OrderDishMapper, OrderDish> implements OrderDishService {
}
