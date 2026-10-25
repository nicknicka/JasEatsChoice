package com.xx.jaseatschoicejava.entity;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {


    /**
     * 手机号（兼容之前的登录方式）
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
     * 短信验证码（短信验证码登录时使用）
     */
    private String code;



}
