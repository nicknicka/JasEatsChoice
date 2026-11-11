package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 管理员解决入参：只接受处理结果编码。
 */
@Data
public class FeedbackResolveDTO {

    private String resolutionCode;
}
