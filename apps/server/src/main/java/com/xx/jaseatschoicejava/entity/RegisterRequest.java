package com.xx.jaseatschoicejava.entity;

import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {
    /**
     * 手机号码
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码会话key
     */
    private String checkCodeKey;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;
}
