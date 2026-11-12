package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈 Mapper。
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
