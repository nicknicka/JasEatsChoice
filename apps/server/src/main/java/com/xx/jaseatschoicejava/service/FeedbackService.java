package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.dto.FeedbackTriageDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackAttachment;
import com.xx.jaseatschoicejava.entity.FeedbackIssueLink;
import com.xx.jaseatschoicejava.entity.FeedbackMessage;
import com.xx.jaseatschoicejava.vo.AdminFeedbackDetailVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    /**
     * 管理端分页列表，可按状态/类型/用户/商家/订单筛选。
     */
    IPage<Feedback> adminList(long page, long size, String status, String type,
                              String userId, String merchantId, String orderId);

    /**
     * 各状态反馈条数，供管理端分诊队列摘要使用。
     */
    Map<String, Long> adminStatusCounts();

    /**
     * 管理端详情：包含内部备注、全部消息、附件与审计。
     */
    AdminFeedbackDetailVO adminDetail(String feedbackId);

    /**
     * 管理员回复用户（USER）或写内部备注（INTERNAL），写入审计。
     */
    FeedbackMessage addAdminMessage(Long adminId, String feedbackId, String content, String visibility);

    /**
     * 分诊：只接受 needs_info / accepted / rejected，按状态机校验；
     * 接受时可判定为平台系统问题。
     */
    Feedback triage(Long adminId, String feedbackId, FeedbackTriageDTO dto);

    /**
     * 为已接受的平台系统反馈创建（或复用）GitHub Issue 关联。
     */
    FeedbackIssueLink createIssue(Long adminId, String feedbackId, FeedbackIssueDraftDTO draft);

    /**
     * 用 GitHub 侧状态回写反馈：关闭时把已接受的反馈置为已解决并留一条用户可见说明。
     */
    Feedback syncIssueState(String feedbackId, String state);

    /**
     * 解决：只允许 accepted -> resolved 并保存结果编码。
     */
    Feedback resolve(Long adminId, String feedbackId, String resolutionCode);
}
