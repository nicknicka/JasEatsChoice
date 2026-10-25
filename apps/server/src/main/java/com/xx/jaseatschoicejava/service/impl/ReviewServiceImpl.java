package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.mapper.ReviewMapper;
import com.xx.jaseatschoicejava.service.ReviewService;
import org.springframework.stereotype.Service;

/**
 * 用户评价服务实现
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
}
