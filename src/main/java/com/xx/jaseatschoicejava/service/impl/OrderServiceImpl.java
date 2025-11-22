package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.mapper.OrderMapper;
import com.xx.jaseatschoicejava.service.OrderService;
import org.springframework.stereotype.Service;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
