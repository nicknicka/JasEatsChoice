package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackAttachment;
import com.xx.jaseatschoicejava.entity.FeedbackMessage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户反馈应用服务：协调反馈聚合、消息、附件、分诊与对外端口。
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

    /**
     * 当前用户可见的消息（不含内部备注），按时间正序。
     */
    List<FeedbackMessage> listVisibleMessages(String feedbackId);

    /**
     * 反馈附件，按时间正序。
     */
    List<FeedbackAttachment> listAttachments(String feedbackId);

    /**
     * 用户在自己的反馈下追加一条用户可见消息。
     */
    FeedbackMessage appendUserMessage(String userId, String feedbackId, String content);

    /**
     * 用户上传一张反馈附件，校验归属、类型、大小与数量。
     */
    FeedbackAttachment addAttachment(String userId, String feedbackId, MultipartFile file);

    /**
     * 管理员修正订单/商家关联，写入审计。
     */
    Feedback updateAssociation(Long adminId, String feedbackId, FeedbackAssociationUpdateDTO dto);
}
