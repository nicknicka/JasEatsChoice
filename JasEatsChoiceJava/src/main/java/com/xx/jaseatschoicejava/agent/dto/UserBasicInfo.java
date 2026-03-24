package com.xx.jaseatschoicejava.agent.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 用户基本信息DTO
 *
 * 用于Agent工具类返回用户基本信息
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class UserBasicInfo {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 会员等级
     */
    private String memberLevel;

    /**
     * 账户状态
     */
    private String status;

    /**
     * 用户是否存在
     */
    private Boolean exists;
}
