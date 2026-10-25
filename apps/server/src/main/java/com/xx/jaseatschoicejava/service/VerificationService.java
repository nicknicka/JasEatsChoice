package com.xx.jaseatschoicejava.service;

import java.util.Map;

/**
 * 验证码Service接口
 */
public interface VerificationService {

    /**
     * 发送验证码
     */
    boolean sendVerificationCode(String phone, String type);

    /**
     * 验证验证码
     */
    boolean verifyCode(String phone, String code, String type);

    /**
     * 生成验证码（6位数字）
     */
    String generateCode();
}
