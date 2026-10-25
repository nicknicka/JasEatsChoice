package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户评价Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
