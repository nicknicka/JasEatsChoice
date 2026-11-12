package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 用户提交反馈的入参。
 *
 * <p>刻意不接收 userId：归属只取可信 JWT。orderId / merchantId / images 由后续 ticket
 * 落地校验，这里先承接字段以保证契约稳定。
 */
@Data
public class FeedbackCreateDTO {

    /** 反馈类型，取值见 FeedbackType。 */
    private String type;

    /** 反馈正文。 */
    private String content;

    /** 联系方式（可选，内部排查用）。 */
    private String contact;

    /** 设备元数据（可选）。 */
    private Map<String, Object> deviceInfo;

    /** 关联订单号（可选）。 */
    private String orderId;

    /** 关联商家ID（可选）。 */
    private String merchantId;

    /** 附件地址列表（可选）。 */
    private List<String> images;
}
