package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员创建 GitHub Issue 时提供的脱敏内容。
 *
 * <p>这里只接受已经脱敏的字段：绝不接受联系方式、订单/支付标识、用户身份或附件正文。
 */
@Data
public class FeedbackIssueDraftDTO {

    /** 脱敏标题。 */
    private String title;

    /** 脱敏摘要。 */
    private String summary;

    private String reproductionSteps;

    private String expectedBehavior;

    private String actualBehavior;

    private String acceptanceCriteria;

    /** 禁止范围（例如不得访问生产数据、不得改动支付流程）。 */
    private String forbiddenScope;

    /** 附加标签，服务端会与分类标签、队列标签合并。 */
    private List<String> labels;
}
