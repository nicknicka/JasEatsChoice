
package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Discount;
import com.xx.jaseatschoicejava.mapper.DiscountMapper;
import com.xx.jaseatschoicejava.service.DiscountService;
import org.springframework.stereotype.Service;

/**
 * 优惠活动Service实现类
 */
@Service
public class DiscountServiceImpl extends ServiceImpl<DiscountMapper, Discount> implements DiscountService {
}
