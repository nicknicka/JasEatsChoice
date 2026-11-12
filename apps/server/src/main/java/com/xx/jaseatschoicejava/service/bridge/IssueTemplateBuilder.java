package com.xx.jaseatschoicejava.service.bridge;

import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.enums.FeedbackType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 把反馈转成脱敏的 GitHub Issue 正文。
 *
 * <p>刻意只使用管理员提供的脱敏字段与反馈的类别/环境：绝不写入用户身份、联系方式、
 * 订单/支付标识、原始日志或附件正文。
 */
public final class IssueTemplateBuilder {

    private IssueTemplateBuilder() {
    }

    /** 隐藏标记，用于对账而不在渲染正文里显眼展示。 */
    public static String hiddenMarker(String feedbackId) {
        return "<!-- feedback:" + feedbackId + " -->";
    }

    /**
     * 构造 Issue 正文。
     */
    public static String build(Feedback feedback, FeedbackIssueDraftDTO draft) {
        FeedbackType type = FeedbackType.fromCode(feedback.getType());
        String category = type == null ? feedback.getType() : type.getLabel();
        StringBuilder sb = new StringBuilder();
        sb.append("> 由用户反馈桥接自动生成，内部反馈引用见文末隐藏标记。请勿在此粘贴任何用户隐私数据。\n\n");
        sb.append("## 分类\n\n").append(safe(category)).append("\n\n");
        sb.append("## 环境\n\n").append(safe(orDefault(feedback.getEnvironment(), "未提供"))).append("\n\n");
        sb.append("## 问题摘要\n\n").append(safe(draft.getSummary())).append("\n\n");
        sb.append("## 复现步骤\n\n").append(block(draft.getReproductionSteps())).append("\n\n");
        sb.append("## 期望行为\n\n").append(safe(draft.getExpectedBehavior())).append("\n\n");
        sb.append("## 实际行为\n\n").append(safe(draft.getActualBehavior())).append("\n\n");
        sb.append("## 验收标准\n\n").append(block(draft.getAcceptanceCriteria())).append("\n\n");
        if (hasText(draft.getForbiddenScope())) {
            sb.append("## 禁止范围\n\n").append(block(draft.getForbiddenScope())).append("\n\n");
        }
        sb.append("## 队列\n\n本 Issue 只是研发队列条目；`@agent` 的显式请求是否放行，由 dsh 的 Source 与 Policy 判定。\n\n");
        sb.append(hiddenMarker(feedback.getFeedbackId())).append("\n");
        return sb.toString();
    }

    /**
     * 合并标签：分类标签 + 队列标签 ready-for-agent + 管理员附加标签，去重保序。
     */
    public static List<String> labels(Feedback feedback, FeedbackIssueDraftDTO draft) {
        Set<String> labels = new LinkedHashSet<>();
        FeedbackType type = FeedbackType.fromCode(feedback.getType());
        if (type != null) {
            labels.add("feedback:" + type.getCode());
        }
        // ready-for-agent 只表示「可被领取」，不是触发条件。
        labels.add("ready-for-agent");
        if (draft.getLabels() != null) {
            for (String label : draft.getLabels()) {
                if (hasText(label)) {
                    labels.add(label.trim());
                }
            }
        }
        return new ArrayList<>(labels);
    }

    private static String safe(String value) {
        return hasText(value) ? value.trim() : "（未提供）";
    }

    private static String block(String value) {
        if (!hasText(value)) {
            return "（未提供）";
        }
        String trimmed = value.trim();
        if (trimmed.contains("\n")) {
            StringBuilder sb = new StringBuilder();
            for (String line : trimmed.split("\n", -1)) {
                sb.append("- ").append(line.strip()).append('\n');
            }
            return sb.toString().stripTrailing();
        }
        return trimmed;
    }

    private static String orDefault(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
