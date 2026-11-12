package com.xx.jaseatschoicejava.service.bridge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.config.GitHubBridgeProperties;
import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackIssueLink;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.FeedbackIssueLinkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 反馈-Issue 桥接实现：一对一幂等创建 + 状态同步。
 */
@Service
public class FeedbackIssueBridgeImpl implements FeedbackIssueBridge {

    @Autowired
    private FeedbackIssueLinkMapper linkMapper;

    @Autowired
    private GitHubIssueClient gitHubIssueClient;

    @Autowired
    private GitHubBridgeProperties properties;

    @Override
    public FeedbackIssueLink createOrGet(Feedback feedback, FeedbackIssueDraftDTO draft) {
        requireDraftComplete(draft);
        // 幂等：已有链接直接复用，不再调用 GitHub。
        FeedbackIssueLink existing = findByFeedbackId(feedback.getFeedbackId());
        if (existing != null) {
            return existing;
        }
        String repo = properties.getRepo();
        String body = IssueTemplateBuilder.build(feedback, draft);
        List<String> labels = IssueTemplateBuilder.labels(feedback, draft);
        GitHubIssueClient.CreatedIssue created =
                gitHubIssueClient.createIssue(repo, title(feedback, draft), body, labels);

        FeedbackIssueLink link = new FeedbackIssueLink();
        link.setFeedbackId(feedback.getFeedbackId());
        link.setRepo(repo);
        link.setIssueNumber(created.number());
        link.setIssueUrl(created.url());
        link.setState("open");
        linkMapper.insert(link);
        return link;
    }

    @Override
    public FeedbackIssueLink syncState(String feedbackId, String state) {
        FeedbackIssueLink link = findByFeedbackId(feedbackId);
        if (link == null) {
            throw new BusinessException("404", "反馈尚未关联 Issue");
        }
        String normalized = "closed".equalsIgnoreCase(state) ? "closed" : "open";
        link.setState(normalized);
        linkMapper.updateById(link);
        return link;
    }

    private FeedbackIssueLink findByFeedbackId(String feedbackId) {
        return linkMapper.selectOne(new LambdaQueryWrapper<FeedbackIssueLink>()
                .eq(FeedbackIssueLink::getFeedbackId, feedbackId));
    }

    private static String title(Feedback feedback, FeedbackIssueDraftDTO draft) {
        if (draft.getTitle() != null && !draft.getTitle().isBlank()) {
            return draft.getTitle().trim();
        }
        return "[反馈] " + feedback.getType();
    }

    private static void requireDraftComplete(FeedbackIssueDraftDTO draft) {
        if (draft == null
                || isBlank(draft.getSummary())
                || isBlank(draft.getReproductionSteps())
                || isBlank(draft.getExpectedBehavior())
                || isBlank(draft.getActualBehavior())
                || isBlank(draft.getAcceptanceCriteria())) {
            throw new BusinessException("400", "反馈规格不完整，无法创建 Issue");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
