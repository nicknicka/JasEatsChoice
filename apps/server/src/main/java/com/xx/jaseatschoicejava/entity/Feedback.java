package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈聚合根。
 *
 * <p>订单号、支付标识、联系方式与设备元数据只作内部排查使用，不进入 GitHub Issue、
 * dsh 上下文或通知正文。
 */
@Data
@TableName("fb_feedback")
public class Feedback {

    @TableId(type = IdType.ASSIGN_ID)
    private String feedbackId;

    /** 提交用户ID（来自可信 JWT，永不由请求体决定）。 */
    private String userId;

    /** 反馈类型编码，见 {@link com.xx.jaseatschoicejava.enums.FeedbackType}。 */
    @TableField("feedback_type")
    private String type;

    private String content;

    /** 生命周期状态编码，见 {@link com.xx.jaseatschoicejava.enums.FeedbackStatus}。 */
    private String status = "pending";

    /** 处理结果编码，见 {@link com.xx.jaseatschoicejava.enums.FeedbackResolutionCode}。 */
    private String resolutionCode;

    private String orderId;

    private String merchantId;

    private String contact;

    /** 设备元数据 JSON 字符串。 */
    private String deviceInfo;

    /** 环境摘要（平台 / 系统 / 版本），供脱敏桥接模板使用。 */
    private String environment;

    /** 是否被分诊判定为平台系统问题（只有这类反馈才能进研发队列）。 */
    private Boolean platformSystem;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
