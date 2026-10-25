package com.xx.jaseatschoicejava.service;

/**
 * 邮件发送服务接口
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     *
     * @param email 收件人邮箱地址
     * @param code  验证码
     * @throws Exception 发送失败时抛出异常
     */
    void sendEmailVerifyCode(String email, String code) throws Exception;

    /**
     * 发送普通文本邮件
     *
     * @param to      收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @throws Exception 发送失败时抛出异常
     */
    void sendTextEmail(String to, String subject, String content) throws Exception;
}
