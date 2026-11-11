package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.entity.Feedback;

/**
 * 用户反馈应用服务。
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 以可信身份提交反馈，状态固定以 pending 落库。
     */
    Feedback create(String userId, FeedbackCreateDTO dto);

    /**
     * 分页列出当前用户自己的反馈。
     */
    IPage<Feedback> listOwn(String userId, long page, long size, String type, String status);

    /**
     * 读取当前用户自己的反馈；不存在或不属于该用户时抛出业务异常。
     */
    Feedback getOwn(String userId, String feedbackId);
}
