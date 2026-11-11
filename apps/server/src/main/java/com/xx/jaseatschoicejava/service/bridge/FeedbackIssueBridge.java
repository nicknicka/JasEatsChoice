package com.xx.jaseatschoicejava.service.bridge;

import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackIssueLink;

/**
 * 反馈到 GitHub Issue 的桥接端口：创建、关联与状态同步。
 */
public interface FeedbackIssueBridge {

    /**
     * 为已接受、规格完整的平台系统反馈创建（或复用）Issue 关联。幂等。
     */
    FeedbackIssueLink createOrGet(Feedback feedback, FeedbackIssueDraftDTO draft);

    /**
     * 用 GitHub 侧状态回写关联，返回更新后的关联。
     */
    FeedbackIssueLink syncState(String feedbackId, String state);
}
