package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 管理员回复/内部备注入参。
 */
@Data
public class AdminFeedbackMessageDTO {

    private String content;

    /** USER 回复用户 / INTERNAL 内部备注。默认 USER。 */
    private String visibility;
}
