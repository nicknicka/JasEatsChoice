package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 管理员分诊入参：target 只接受 needs_info / accepted / rejected。
 */
@Data
public class FeedbackTriageDTO {

    private String status;
}
