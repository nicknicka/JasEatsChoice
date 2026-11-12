package com.xx.jaseatschoicejava.vo;

import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.enums.FeedbackPriority;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 管理端反馈列表项：在用户可见视图之外，附带分诊决策字段。
 *
 * <p>{@link FeedbackVO} 同时服务用户端，所以优先级与负责人不能加在它身上——
 * 那会把内部字段透给用户接口。这两个字段也不要进 GitHub 桥接的脱敏模板。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminFeedbackVO extends FeedbackVO {

    /** 分诊优先级：normal / important / urgent。 */
    private String priority;

    private String priorityLabel;

    /** 当前负责人 sys_admin.admin_id；未认领为空。 */
    private Long assigneeId;

    /** 负责人展示名：real_name 回落 username；未认领为空。 */
    private String assigneeName;

    public static AdminFeedbackVO of(Feedback feedback, String assigneeName) {
        AdminFeedbackVO vo = new AdminFeedbackVO();
        vo.fill(feedback);
        vo.setMessages(List.of());
        vo.setAttachments(List.of());
        vo.priority = feedback.getPriority();
        vo.priorityLabel = FeedbackPriority.labelOf(feedback.getPriority());
        vo.assigneeId = feedback.getAssigneeId();
        vo.assigneeName = assigneeName;
        return vo;
    }
}
