package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.ReviewReply;
import com.xx.jaseatschoicejava.mapper.ReviewReplyMapper;
import com.xx.jaseatschoicejava.service.ReviewReplyService;
import org.springframework.stereotype.Service;

/**
 * 评价回复服务实现
 */
@Service
public class ReviewReplyServiceImpl extends ServiceImpl<ReviewReplyMapper, ReviewReply> implements ReviewReplyService {
}
