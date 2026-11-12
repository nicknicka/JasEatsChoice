package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 管理员分诊入参：target 只接受 needs_info / accepted / rejected。
 */
@Data
public class FeedbackTriageDTO {

    private String status;

    /** 接受时是否判定为平台系统问题（决定能否进入 GitHub 桥接）。 */
    private Boolean platformSystem;
}
